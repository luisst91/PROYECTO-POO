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
    jugadores: List[str] = Field(min_length=2, max_length=2)

@app.post("/partidas")
def crear_partida(request: CrearPartidaRequest):
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
    with lock_db:
        db = cargar_db()
    partida = db["partidas"].get(partida_id)
    if partida is None:
        raise HTTPException(status_code=404, detail="Partida no encontrada")
    return partida

@app.get("/partidas/{partida_id}/templos/{coord}")
def obtener_templo(partida_id: str, coord: str):
    partida = buscar_partida(partida_id)
    templo = partida["secretos"]["templos"].get(coord.upper())
    if templo is None:
        raise HTTPException(status_code=404, detail="Ahi no hay un templo normal")
    return templo

@app.get("/partidas/{partida_id}/cofre")
def obtener_cofre(partida_id: str):
    partida = buscar_partida(partida_id)
    return partida["secretos"]["cofre"]


class JugadorRequest(BaseModel):
    nombre: str
    alias: str

class GuardarEstadoRequest(BaseModel):
    revisionBase: int
    estado: dict


@app.get("/salud")
def consultar_salud():
    return {"disponible": True, "versionContrato": 2}

@app.post("/jugadores", status_code=201)
def registrar_jugador(request: JugadorRequest):
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
    with lock_db:
        db = cargar_db()

    lista = []
    for jugador in db["jugadores"].values():
        if alias.lower() in jugador["alias"].lower():
            lista.append(jugador)
    return lista

@app.get("/jugadores/{jugador_id}/estadisticas")
def estadisticas_jugador(jugador_id: str):
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
    
    return {"mensaje": "Partida generada con éxito", "partida": nueva_partida}
