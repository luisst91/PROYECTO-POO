"""
* usa FastAPI para crear la ruta "POST /partidas" que conecta por donde el programa en Java va a pedir crear los juegos
* recibe la lista de alias de los jugadores y le inventa un ID unico a esa partida (tipo PARTIDA-A1B2C3D4)
* llama a tu función de generador_mapa.py para que fabrique un tablero nuevo en el momento
* guarda todo el mapa, las coordenadas, el cofre cifrado y las pistas en un archivo json
* manda esos datos ordenados a storage.py para que se guarden
* por ultimo le devuelve todo el mapa y los datos listos al cliente para que puedan empezar a jugar
"""

import copy
import random
import re
import threading
import uuid
from typing import Any

from fastapi import FastAPI, HTTPException, Request
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse
from pydantic import BaseModel, Field, field_validator

from storage import cargar_db, guardar_db
from generador_mapa import generar_partida


app = FastAPI(title="API Expedición Isla de las Reliquias", version="2.0.0", description="Backend para la gestión de jugadores y partidas.")

# evita que dos peticiones al mismo tiempo se pisen al guardar el json
lock_db = threading.RLock()
BASE = "/api/v1"


class ErrorContrato(Exception):
    def __init__(self, status, codigo, mensaje):
        self.status = status
        self.codigo = codigo
        self.mensaje = mensaje


@app.exception_handler(ErrorContrato)
def manejar_error(request: Request, exc: ErrorContrato):
    return JSONResponse(status_code=exc.status, content={"codigo": exc.codigo, "mensaje": exc.mensaje})


@app.exception_handler(HTTPException)
def manejar_http(request: Request, exc: HTTPException):
    return JSONResponse(status_code=exc.status_code, content={"codigo": "ERROR_HTTP", "mensaje": str(exc.detail)})


@app.exception_handler(RequestValidationError)
def manejar_validacion(request: Request, exc: RequestValidationError):
    return JSONResponse(status_code=400, content={"codigo": "DATOS_INVALIDOS", "mensaje": "Solicitud inválida; revise los campos y tipos"})


def error(status, codigo, mensaje):
    raise ErrorContrato(status, codigo, mensaje)


class CrearJugador(BaseModel):
    nombre: str = Field(min_length=1, max_length=80)
    alias: str = Field(min_length=3, max_length=20)

    @field_validator("alias")
    @classmethod
    def alias_valido(cls, valor):
        if not re.fullmatch(r"[A-Za-z0-9_-]{3,20}", valor):
            raise ValueError("Alias inválido")
        return valor


class CrearPartida(BaseModel):
    requestId: str = Field(min_length=1)
    jugadores: list[str] = Field(min_length=2, max_length=2)


class GuardarEstado(BaseModel):
    revisionBase: int = Field(ge=0)
    estado: dict[str, Any]


def buscar_partida(db, partida_id):
    partida = db["partidas"].get(partida_id)

    if partida is None:
        error(404, "PARTIDA_NO_ENCONTRADA", "La partida no existe")

    return partida


def resumen(partida):
    return {
        "partidaId": partida["partidaId"],
        "jugadores": partida["configuracion"]["jugadores"],
        "estado": partida["estado"]["estado"],
        "ronda": partida["estado"]["ronda"]
    }


def publica(partida):
    return {
        "partidaId": partida["partidaId"],
        "configuracion": copy.deepcopy(partida["configuracion"]),
        "estado": copy.deepcopy(partida["estado"]),
        "revision": partida["revision"]
    }


@app.get(BASE + "/salud", summary="Comprobar disponibilidad del servicio")
def salud():
    return {"disponible": True, "versionContrato": 2}


@app.post(BASE + "/jugadores", status_code=201, summary="Registrar un jugador")
def registrar_jugador(request: CrearJugador):
    with lock_db:
        db = cargar_db()

        alias_existente = any(jugador["alias"].casefold() == request.alias.casefold() for jugador in db["jugadores"].values())

        if alias_existente:
            error(409, "ALIAS_DUPLICADO", "El alias ya está registrado")

        jugador_id = "J-" + uuid.uuid4().hex[:12].upper()
        jugador = {"jugadorId": jugador_id, "nombre": request.nombre, "alias": request.alias}

        db["jugadores"][jugador_id] = jugador
        guardar_db(db)

        return jugador


@app.get(BASE + "/jugadores", summary="Listar jugadores registrados")
def listar_jugadores(alias: str | None = None):
    with lock_db:
        jugadores = list(cargar_db()["jugadores"].values())

    if alias is not None:
        jugadores = [jugador for jugador in jugadores if jugador["alias"].casefold() == alias.casefold()]

    return {"jugadores": jugadores}


@app.get(BASE + "/jugadores/{jugador_id}/estadisticas", summary="Consultar estadísticas finales")
def estadisticas(jugador_id: str):
    with lock_db:
        db = cargar_db()

        if jugador_id not in db["jugadores"]:
            error(404, "JUGADOR_NO_ENCONTRADO", "El jugador no existe")

        partidas = [
            partida for partida in db["partidas"].values()
            if partida["estado"].get("estado") == "FINALIZADA" and jugador_id in partida["configuracion"]["jugadores"]
        ]

        victorias = sum(jugador_id in partida["estado"].get("ganadores", []) for partida in partidas)
        puntos = 0
        reliquias = 0

        for partida in partidas:
            for jugador in partida["estado"].get("jugadores", []):
                if jugador.get("jugadorId") == jugador_id:
                    puntos += jugador.get("puntos", 0)
                    reliquias += len(jugador.get("entregadas", []))

        return {
            "jugadorId": jugador_id,
            "partidasFinalizadas": len(partidas),
            "victorias": victorias,
            "puntosAcumulados": puntos,
            "reliquiasEntregadas": reliquias
        }


@app.post(BASE + "/partidas", status_code=201, summary="Generar una partida")
def crear_partida(request: CrearPartida):
    with lock_db:
        db = cargar_db()
        anterior = db["requests_partidas"].get(request.requestId)

        if anterior:
            if anterior["jugadores"] != request.jugadores:
                error(409, "REQUEST_ID_CONFLICTIVO", "requestId reutilizado con otros jugadores")

            return publica(buscar_partida(db, anterior["partidaId"]))

        if request.jugadores[0] == request.jugadores[1] or any(jugador not in db["jugadores"] for jugador in request.jugadores):
            error(400, "JUGADORES_INVALIDOS", "Se requieren dos jugadores registrados y distintos")

        previas = [partida for partida in db["partidas"].values() if set(partida["configuracion"]["jugadores"]) == set(request.jugadores)]
        previa = previas[-1] if previas else None
        partida_id = "EX-" + uuid.uuid4().hex[:12].upper()

        _, datos = generar_partida(
            partida_id=partida_id,
            campamento_anterior=previa["configuracion"]["campamento"] if previa else None,
            cofre_anterior=previa["configuracion"]["cofre"] if previa else None,
            pistas_anteriores=previa["secretos"]["pistasGeneradas"] if previa else None
        )

        if datos is None:
            error(503, "GENERACION_FALLIDA", "No se generó un escenario válido en 1000 intentos")

        orden = request.jugadores.copy()
        random.SystemRandom().shuffle(orden)

        estado = {
            "estado": "EN_CURSO",
            "ronda": 1,
            "indiceJugadorActual": 0,
            "accionesRestantes": 3,
            "jugadores": [
                {"jugadorId": jugador, "posicion": datos["campamento"], "puntos": 0, "inventario": [], "entregadas": []}
                for jugador in orden
            ],
            "templosExplorados": [],
            "pistas": [],
            "cofreAbierto": False,
            "ganadores": []
        }

        partida = {
            "partidaId": partida_id,
            "configuracion": {
                "tablero": datos["matriz"],
                "casillas": datos["casillas"],
                "campamento": datos["campamento"],
                "cofre": datos["cofre"],
                "jugadores": request.jugadores,
                "ordenJugadores": orden
            },
            "secretos": {
                "templos": datos["secretos_templos"],
                "cofre": datos["paquete_cofre"],
                "pistasGeneradas": datos["pistas"]
            },
            "estado": estado,
            "revision": 0,
            "ultimoGuardado": None
        }

        db["partidas"][partida_id] = partida
        db["requests_partidas"][request.requestId] = {"jugadores": request.jugadores, "partidaId": partida_id}

        # Guardamos en el storage
        guardar_db(db)

        return publica(partida)


@app.get(BASE + "/partidas", summary="Listar partidas")
def listar_partidas(jugadorId: str | None = None):
    with lock_db:
        partidas = list(cargar_db()["partidas"].values())

    if jugadorId:
        partidas = [partida for partida in partidas if jugadorId in partida["configuracion"]["jugadores"]]

    return {"partidas": [resumen(partida) for partida in partidas]}


@app.get(BASE + "/partidas/{partida_id}", summary="Recuperar partida y estado")
def obtener_partida(partida_id: str):
    with lock_db:
        db = cargar_db()
        partida = buscar_partida(db, partida_id)
        return publica(partida)


@app.get(BASE + "/partidas/{partida_id}/templos/{coord}", summary="Consultar contenido de un templo")
def obtener_templo(partida_id: str, coord: str):
    with lock_db:
        db = cargar_db()
        partida = buscar_partida(db, partida_id)
        templo = partida["secretos"]["templos"].get(coord.upper())

        if templo is None:
            error(404, "TEMPLO_NO_ENCONTRADO", "No hay templo en esa coordenada")

        return copy.deepcopy(templo)


@app.get(BASE + "/partidas/{partida_id}/cofre", summary="Consultar paquete cifrado")
def obtener_cofre(partida_id: str):
    with lock_db:
        db = cargar_db()
        partida = buscar_partida(db, partida_id)
        return copy.deepcopy(partida["secretos"]["cofre"])


@app.put(BASE + "/partidas/{partida_id}/estado", summary="Guardar estado con revisión optimista")
def guardar_estado(partida_id: str, request: GuardarEstado):
    with lock_db:
        db = cargar_db()
        partida = buscar_partida(db, partida_id)
        estado = request.estado
        base = request.revisionBase

        if partida.get("ultimoGuardado") == {"revisionBase": base, "estado": estado}:
            return {"revision": partida["revision"], "guardado": True}

        if base != partida["revision"]:
            error(409, "REVISION_CONFLICTIVA", "El estado fue modificado desde otra revisión")

        if partida["estado"]["estado"] == "FINALIZADA":
            error(409, "PARTIDA_FINALIZADA", "No se permite modificar una partida finalizada")

        validar_snapshot(partida, estado)

        partida["estado"] = copy.deepcopy(estado)
        partida["revision"] += 1
        partida["ultimoGuardado"] = {"revisionBase": base, "estado": copy.deepcopy(estado)}

        guardar_db(db)
        return {"revision": partida["revision"], "guardado": True}


def validar_snapshot(partida, estado):
    campos = {"estado", "ronda", "indiceJugadorActual", "accionesRestantes", "jugadores", "templosExplorados", "pistas", "cofreAbierto", "ganadores"}

    if not campos.issubset(estado):
        error(400, "ESTADO_INVALIDO", "Faltan campos obligatorios en el snapshot")

    if estado["estado"] not in ("EN_CURSO", "FINALIZADA") or type(estado["ronda"]) is not int or not 1 <= estado["ronda"] <= 8:
        error(400, "ESTADO_INVALIDO", "Estado o ronda inválidos")

    if type(estado["indiceJugadorActual"]) is not int or estado["indiceJugadorActual"] not in (0, 1):
        error(400, "ESTADO_INVALIDO", "Índice de jugador inválido")

    if type(estado["accionesRestantes"]) is not int or not 0 <= estado["accionesRestantes"] <= 3:
        error(400, "ESTADO_INVALIDO", "Presupuesto de acciones inválido")

    if estado["estado"] == "FINALIZADA" and (estado["ronda"] != 8 or estado["accionesRestantes"] != 0):
        error(400, "ESTADO_INVALIDO", "Una partida finalizada debe cerrar la ronda ocho")

    if not isinstance(estado["jugadores"], list) or len(estado["jugadores"]) != 2:
        error(400, "ESTADO_INVALIDO", "Se requieren dos jugadores")

    if [jugador.get("jugadorId") for jugador in estado["jugadores"]] != partida["configuracion"]["ordenJugadores"]:
        error(400, "ESTADO_INVALIDO", "Los jugadores o su orden no coinciden")

    coords = {casilla["coordenada"] for casilla in partida["configuracion"]["casillas"]}

    for jugador in estado["jugadores"]:
        if jugador.get("posicion") not in coords or type(jugador.get("puntos")) is not int or jugador["puntos"] < 0:
            error(400, "ESTADO_INVALIDO", "Posición o puntos inválidos")

        if not isinstance(jugador.get("inventario"), list) or len(jugador["inventario"]) > 2 or not isinstance(jugador.get("entregadas"), list):
            error(400, "ESTADO_INVALIDO", "Inventario o entregas inválidos")

    if not isinstance(estado["templosExplorados"], list) or not set(estado["templosExplorados"]).issubset(partida["secretos"]["templos"]):
        error(400, "ESTADO_INVALIDO", "Templos explorados inválidos")

    if not isinstance(estado["pistas"], list) or not isinstance(estado["cofreAbierto"], bool):
        error(400, "ESTADO_INVALIDO", "Pistas o cofre inválidos")

    if not isinstance(estado["ganadores"], list) or not set(estado["ganadores"]).issubset(partida["configuracion"]["jugadores"]):
        error(400, "ESTADO_INVALIDO", "Ganadores inválidos")
