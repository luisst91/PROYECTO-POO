from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field
from typing import List
import uuid
import threading

from storage import cargar_db, guardar_db
from generador_mapa import generar_partida

"""
* usa FastAPI para crear la ruta "POST /partidas" que conecta por donde el programa en Java va a pedir crear los juegos
* recibe la lista de alias de los jugadores y le inventa un ID unico a esa partida (tipo PARTIDA-A1B2C3D4)
* llama a tu función de generador_mapa.py para que fabrique un tablero nuevo en el momento
* guarda todo el mapa, las coordenadas, el cofre cifrado y las pistas en un archivo json
* manda esos datos ordenados a storage.py para que se guarden
* por ultimo le devuelve todo el mapa y los datos listos al cliente para que puedan empezar a jugar
"""

app = FastAPI(title="API Isla de las Reliquias")

# evita que dos peticiones al mismo tiempo se pisen al guardar el json
lock_db = threading.Lock()

class CrearPartidaRequest(BaseModel):
    """
    Cuerpo que debe mandar Java en POST /partidas.
    Attributes: jugadores: Lista con exactamente dos nombres de jugador (minimo 2 y maximo 2).
    """
    jugadores: List[str] = Field(min_length=2, max_length=2)

@app.post("/partidas")
def crear_partida(request: CrearPartidaRequest):
    """
    Endpoint POST /partidas: crea una partida nueva para dos jugadores.
    Que hace paso a paso:
        1. Inventa un ID unico para la partida (tipo PARTIDA-A1B2C3D4).
        2. Llama a generar_partida() de generador_mapa.py para fabricar el tablero 5X5, las reliquias,
           las pistas y el cofre cifrado.
        3. Sortea con random.shuffle() el orden en que van a jugar los dos jugadores.
        4. Arma la partida en dos partes: "configuracion" (lo publico: tablero, campamento, cofre y jugadores)
           y "secretos" (lo que solo conoce el servidor: reliquias de los templos, paquete cifrado y pistas).
        5. Guarda la partida en el archivo JSON usando el candado lock_db para que dos peticiones
           no se pisen al guardar.
        6. Devuelve a Java solo la configuracion; los secretos se piden despues, cuando hacen falta.
    Args: request: Cuerpo de la peticion con la lista de dos jugadores.
    Returns:Un diccionario con el mensaje de exito, el partidaId y la configuracion de la partida. Si no se pudo generar un mapa valido responde con error 500.
    """
    partida_id = f"PARTIDA-{str(uuid.uuid4())[:8].upper()}"
    
    _, datos_partida = generar_partida(partida_id=partida_id)
    
    if datos_partida is None:
        raise HTTPException(status_code=500, detail="No se pudo generar un mapa valido")

    # la API sortea quien juega primero
    orden_jugadores = list(request.jugadores)
    random.shuffle(orden_jugadores)
    
    nueva_partida = {
        "partidaId": partida_id,
        "configuracion": {
            "tablero": datos_partida["matriz"],
            "casillas": datos_partida["casillas"],
            "campamento": datos_partida["campamento"],
            "cofre": datos_partida["cofre"],
            "jugadores": request.jugadores
        },
        "secretos": {
            "templos": datos_partida["secretos_templos"],
            "cofre": datos_partida["paquete_cofre"],
            "pistasGeneradas": datos_partida["pistas"]
        }
    }
    
    # Guardamos en el storage
    with lock_db:
        db = cargar_db()
        db["partidas"][partida_id] = nueva_partida
        guardar_db(db)

    return {
        "mensaje": "Partida generada con éxito",
        "partida": {
            "partidaId": partida_id,
            "configuracion": nueva_partida["configuracion"]
        }
    }

def buscar_partida(partida_id: str):
    """
    Funcion auxiliar (no es un endpoint): busca una partida guardada en el archivo JSON por su ID.
    La usan los endpoints de templos y cofre para no repetir el mismo codigo.

    Args: partida_id: Identificador de la partida, por ejemplo PARTIDA-A1B2C3D4.

    Returns: El diccionario con todos los datos de la partida (configuracion y secretos). Si no existe responde con error 404 "Partida no encontrada".
    """
    with lock_db:
        db = cargar_db()
    partida = db["partidas"].get(partida_id)
    if partida is None:
        raise HTTPException(status_code=404, detail="Partida no encontrada")
    return partida

@app.get("/partidas/{partida_id}/templos/{coord}")
def obtener_templo(partida_id: str, coord: str):
    """
    Endpoint GET /partidas/{partida_id}/templos/{coord}: devuelve el contenido secreto de un templo.
    Java lo llama cuando un jugador explora un templo para saber que reliquia hay y si tiene pista.

    Args: partida_id: Identificador de la partida. Coord: Coordenada del templo, por ejemplo B3 (se convierte a mayusculas).

    Returns: El diccionario del templo con su coordenada, su reliquia (id y valor) y su pista (indice y fragmento, o null si ese templo no tiene pista).
        Si en esa coordenada no hay un templo normal responde con error 404.
    """
    partida = buscar_partida(partida_id)
    templo = partida["secretos"]["templos"].get(coord.upper())
    if templo is None:
        raise HTTPException(status_code=404, detail="Ahi no hay un templo normal")
    return templo

@app.get("/partidas/{partida_id}/cofre")
def obtener_cofre(partida_id: str):
    """
    Endpoint GET /partidas/{partida_id}/cofre: devuelve el paquete cifrado del cofre.
    Java lo usa cuando el jugador intenta abrir el cofre: con la contrasena que escribe el jugador
    (pista 1 + guion + pista 2) descifra este paquete. No incluye la contrasena ni la clave.

    Args: partida_id: Identificador de la partida.

    Returns: El paquete cifrado con la sal, el nonce, el AAD y el texto cifrado en Base64.
    """
    partida = buscar_partida(partida_id)
    return partida["secretos"]["cofre"]


class JugadorRequest(BaseModel):
    """
    Cuerpo que debe mandar el cliente en POST /jugadores.

    Attributes: nombre: Nombre del jugador (de 1 a 80 caracteres).
        alias: Alias unico del jugador (de 3 a 20 caracteres: letras, digitos, - o _).
    """
    nombre: str
    alias: str

class GuardarEstadoRequest(BaseModel):
    """
    Cuerpo que debe mandar el cliente en PUT /partidas/{id}/estado.

    Attributes: revisionBase: Numero de revision sobre el que se construyo este estado (sirve para detectar conflictos).
        estado: Copia completa (snapshot) del estado de la partida: ronda, turno, jugadores, pistas, etc.
    """
    revisionBase: int
    estado: dict


@app.get("/salud")
def consultar_salud():
    """
    Endpoint GET /salud: sirve para comprobar que el servidor esta encendido y funcionando.

    Returns: Un diccionario con disponible en True y la version del contrato de la API (2).
    """
    return {"disponible": True, "versionContrato": 2}

@app.post("/jugadores", status_code=201)
def registrar_jugador(request: JugadorRequest):
    """
    Endpoint POST /jugadores: registra un jugador nuevo.
    Que hace paso a paso:
        1. Limpia los espacios del nombre y del alias.
        2. Valida que el nombre tenga de 1 a 80 caracteres y el alias de 3 a 20 caracteres
           usando solo letras, digitos, guion o guion bajo. Si no cumple responde 400.
        3. Revisa que el alias no este repetido (sin distinguir mayusculas). Si ya existe responde 409.
        4. Genera el ID del jugador (J1, J2, J3...), lo guarda en el archivo JSON y lo devuelve.
        
    Args: request: Cuerpo de la peticion con el nombre y el alias.

    Returns: El jugador creado con su jugadorId, nombre y alias (codigo 201).
    """
    nombre = request.nombre.strip()
    alias = request.alias.strip()

    if len(nombre) < 1 or len(nombre) > 80:
        return JSONResponse(status_code=400, content={"codigo": "BAD_REQUEST", "mensaje": "El nombre debe tener de 1 a 80 caracteres"})
    if len(alias) < 3 or len(alias) > 20:
        return JSONResponse(status_code=400, content={"codigo": "BAD_REQUEST", "mensaje": "El alias debe tener de 3 a 20 caracteres"})
    for letra in alias:
        if not (letra.isascii() and (letra.isalnum() or letra == "-" or letra == "_")):
            return JSONResponse(status_code=400, content={"codigo": "BAD_REQUEST", "mensaje": "El alias solo puede tener letras, digitos, - o _"})

    with lock_db:
        db = cargar_db()
        for jugador in db["jugadores"].values():
            if jugador["alias"].lower() == alias.lower():
                return JSONResponse(status_code=409, content={"codigo": "CONFLICT", "mensaje": "Ese alias ya esta registrado"})

        jugador_id = f"J{len(db['jugadores']) + 1}"
        jugador = {"jugadorId": jugador_id, "nombre": nombre, "alias": alias}
        db["jugadores"][jugador_id] = jugador
        guardar_db(db)

    return jugador

@app.get("/jugadores")
def listar_jugadores(alias: str = ""):
    """
    Endpoint GET /jugadores: lista los jugadores registrados.
    Si se manda el parametro ?alias=texto solo devuelve los jugadores cuyo alias contenga ese texto
    (sin distinguir mayusculas). Si no se manda, devuelve todos.

    Args: alias: Texto opcional para filtrar por alias.

    Returns: Una lista con los jugadores encontrados.
    """
    with lock_db:
        db = cargar_db()

    lista = []
    for jugador in db["jugadores"].values():
        if alias.lower() in jugador["alias"].lower():
            lista.append(jugador)
    return lista

@app.get("/jugadores/{jugador_id}/estadisticas")
def estadisticas_jugador(jugador_id: str):
    """
    Endpoint GET /jugadores/{jugador_id}/estadisticas: calcula las estadisticas de un jugador.
    Recorre las partidas guardadas que ya tienen resultado final y suma, para ese jugador,
    las partidas finalizadas, las victorias, los puntos acumulados y las reliquias entregadas.
    Cada partida solo cuenta una vez porque el resultado se fija en el primer guardado FINALIZADA.

    Args: jugador_id: Identificador del jugador, por ejemplo J1.

    Returns: Un diccionario con partidasFinalizadas, victorias, puntosAcumulados y reliquiasEntregadas. Si el jugador no existe responde con error 404.
    """
    with lock_db:
        db = cargar_db()

    jugador = db["jugadores"].get(jugador_id)
    if jugador is None:
        return JSONResponse(status_code=404, content={"codigo": "NOT_FOUND", "mensaje": "Jugador no encontrado"})

    finalizadas = 0
    victorias = 0
    puntos = 0
    entregadas = 0
    for partida in db["partidas"].values():
        resultado = partida.get("resultado")
        if resultado is None:
            continue
        for nombre in [jugador_id, jugador["alias"]]:
            if nombre in resultado["jugadores"]:
                finalizadas += 1
                puntos += resultado["jugadores"][nombre]["puntos"]
                entregadas += resultado["jugadores"][nombre]["reliquiasEntregadas"]
                if nombre in resultado["ganadores"]:
                    victorias += 1
                break

    return {
        "jugadorId": jugador_id,
        "alias": jugador["alias"],
        "partidasFinalizadas": finalizadas,
        "victorias": victorias,
        "puntosAcumulados": puntos,
        "reliquiasEntregadas": entregadas
    }

@app.get("/partidas")
def listar_partidas(jugadorId: str = ""):
    """
    Endpoint GET /partidas: lista un resumen de las partidas guardadas.
    Si se manda el parametro ?jugadorId=valor solo devuelve las partidas donde participa ese jugador.

    Args: jugadorId: Valor opcional para filtrar por jugador.

    Returns: Una lista con el partidaId, los jugadores, el estado (EN_CURSO por defecto), la ronda y la revision de cada partida.
    """
    with lock_db:
        db = cargar_db()

    lista = []
    for p in db["partidas"].values():
        if jugadorId != "" and jugadorId not in p["configuracion"]["jugadores"]:
            continue
        estado = p.get("estado")
        lista.append({
            "partidaId": p["partidaId"],
            "jugadores": p["configuracion"]["jugadores"],
            "estado": estado["estado"] if estado else "EN_CURSO",
            "ronda": estado["ronda"] if estado else 1,
            "revision": p.get("revision", 0)
        })
    return lista

@app.get("/partidas/{partida_id}")
def obtener_partida(partida_id: str):
    """
    Endpoint GET /partidas/{partida_id}: recupera una partida concreta.
    Java lo usa para cargar una partida y para confirmar un guardado cuya respuesta se perdio.
    Solo devuelve datos publicos; los secretos (reliquias, pistas y cofre) no salen por aqui.

    Args: partida_id: Identificador de la partida.

    Returns: Un diccionario con el partidaId, la revision, la configuracion y el ultimo estado guardado (null si todavia no se ha guardado ninguno). Si la partida no existe responde con error 404.
    """
    with lock_db:
        db = cargar_db()

    partida = db["partidas"].get(partida_id)
    if partida is None:
        return JSONResponse(status_code=404, content={"codigo": "NOT_FOUND", "mensaje": "Partida no encontrada"})

    return {
        "partidaId": partida_id,
        "revision": partida.get("revision", 0),
        "configuracion": partida["configuracion"],
        "estado": partida.get("estado")
    }

@app.put("/partidas/{partida_id}/estado")
def guardar_estado(partida_id: str, request: GuardarEstadoRequest):
    """
    Endpoint PUT /partidas/{partida_id}/estado: guarda una copia completa (snapshot) del estado de la partida.
    Usa el campo revisionBase para evitar que se pise un guardado mas nuevo o que se duplique uno repetido.
    Que hace paso a paso:
        1. Busca la partida. Si no existe responde 404.
        2. Si es exactamente el mismo guardado de antes (misma revisionBase y mismo estado) es un reintento:
           devuelve la misma confirmacion sin guardar otra vez.
        3. Si revisionBase no coincide con la revision actual responde 409 (conflicto), porque el cliente
           esta trabajando sobre una version vieja.
        4. Revisa que el estado traiga los campos principales. Si falta alguno responde 400.
        5. Guarda el estado y sube la revision en 1.
        6. Si el estado es FINALIZADA y todavia no habia resultado, fija el resultado definitivo:
           gana quien tenga mas puntos, en empate quien entrego mas reliquias, y si tambien empatan ganan ambos.
           Con ese resultado se calculan las estadisticas.
    Args: partida_id: Identificador de la partida.
        request: Cuerpo con la revisionBase y el estado completo.

    Returns: Un diccionario con el partidaId y la nueva revision.
    """
    estado = request.estado

    with lock_db:
        db = cargar_db()

        partida = db["partidas"].get(partida_id)
        if partida is None:
            return JSONResponse(status_code=404, content={"codigo": "NOT_FOUND", "mensaje": "Partida no encontrada"})

        revision_actual = partida.get("revision", 0)
        ultimo = partida.get("ultimoGuardado")

        if ultimo is not None and ultimo["revisionBase"] == request.revisionBase and ultimo["estado"] == estado:
            return {"partidaId": partida_id, "revision": ultimo["revisionResultante"]}

        if request.revisionBase != revision_actual:
            return JSONResponse(status_code=409, content={"codigo": "CONFLICT", "mensaje": "La revision actual es " + str(revision_actual)})

        for campo in ["estado", "ronda", "indiceJugadorActual", "accionesRestantes", "jugadores"]:
            if campo not in estado:
                return JSONResponse(status_code=400, content={"codigo": "BAD_REQUEST", "mensaje": "Falta el campo " + campo})

        nueva_revision = revision_actual + 1
        partida["estado"] = estado
        partida["revision"] = nueva_revision
        partida["ultimoGuardado"] = {
            "revisionBase": request.revisionBase,
            "estado": estado,
            "revisionResultante": nueva_revision
        }

        if estado["estado"] == "FINALIZADA" and "resultado" not in partida:
            mejor = None
            for j in estado["jugadores"]:
                marcador = (j["puntos"], len(j["entregadas"]))
                if mejor is None or marcador > mejor:
                    mejor = marcador

            resultado_jugadores = {}
            ganadores = []
            for j in estado["jugadores"]:
                resultado_jugadores[j["jugadorId"]] = {
                    "puntos": j["puntos"],
                    "reliquiasEntregadas": len(j["entregadas"])
                }
                if (j["puntos"], len(j["entregadas"])) == mejor:
                    ganadores.append(j["jugadorId"])

            partida["resultado"] = {"jugadores": resultado_jugadores, "ganadores": ganadores}

        guardar_db(db)

    return {"partidaId": partida_id, "revision": nueva_revision}
