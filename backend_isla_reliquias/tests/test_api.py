import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "API"))

import pytest
from fastapi.testclient import TestClient
import storage
from main import app


@pytest.fixture
def client(tmp_path, monkeypatch):
    monkeypatch.setattr(storage, "DATA_DIR", tmp_path)
    monkeypatch.setattr(storage, "DB_FILE", tmp_path / "db.json")
    return TestClient(app)


def test_flujo(client):
    respuesta = client.get("/api/v1/salud")
    assert respuesta.status_code == 200
    assert respuesta.json() == {"disponible": True, "versionContrato": 2}

    jugador1 = client.post("/api/v1/jugadores", json={"nombre": "Ana", "alias": "ana_1"})
    jugador2 = client.post("/api/v1/jugadores", json={"nombre": "Luis", "alias": "luis_1"})

    assert jugador1.status_code == 201
    assert jugador2.status_code == 201
    assert client.post("/api/v1/jugadores", json={"nombre": "Otra", "alias": "ANA_1"}).status_code == 409

    ids = [jugador1.json()["jugadorId"], jugador2.json()["jugadorId"]]

    assert len(client.get("/api/v1/jugadores").json()["jugadores"]) == 2
    assert len(client.get("/api/v1/jugadores?alias=ANA_1").json()["jugadores"]) == 1
    assert client.get("/api/v1/jugadores/J-INEXISTENTE/estadisticas").status_code == 404

    solicitud = {"requestId": "req-001", "jugadores": ids}
    respuesta = client.post("/api/v1/partidas", json=solicitud)

    assert respuesta.status_code == 201, respuesta.text

    partida = respuesta.json()
    partida_id = partida["partidaId"]

    assert "secretos" not in partida
    assert partida["revision"] == 0
    assert len(partida["configuracion"]["casillas"]) == 25

    repetida = client.post("/api/v1/partidas", json=solicitud)
    assert repetida.status_code == 201
    assert repetida.json()["partidaId"] == partida_id

    conflicto = client.post("/api/v1/partidas", json={"requestId": "req-001", "jugadores": ids[::-1]})
    assert conflicto.status_code == 409

    assert len(client.get("/api/v1/partidas").json()["partidas"]) == 1
    assert client.get(f"/api/v1/partidas/{partida_id}").status_code == 200
    assert client.get("/api/v1/partidas/INEXISTENTE").status_code == 404

    coordenada = next(casilla["coordenada"] for casilla in partida["configuracion"]["casillas"] if casilla["tipo"] == "T")

    templo = client.get(f"/api/v1/partidas/{partida_id}/templos/{coordenada}")
    cofre = client.get(f"/api/v1/partidas/{partida_id}/cofre")

    assert templo.status_code == 200
    assert cofre.status_code == 200
    assert templo.json()["coordenada"] == coordenada
    assert client.get(f"/api/v1/partidas/{partida_id}/templos/Z9").status_code == 404

    estado = partida["estado"]
    guardado = client.put(f"/api/v1/partidas/{partida_id}/estado", json={"revisionBase": 0, "estado": estado})

    assert guardado.status_code == 200
    assert guardado.json()["revision"] == 1

    repetido = client.put(f"/api/v1/partidas/{partida_id}/estado", json={"revisionBase": 0, "estado": estado})
    assert repetido.status_code == 200
    assert repetido.json()["revision"] == 1

    cambiado = dict(estado, ronda=2)
    conflicto = client.put(f"/api/v1/partidas/{partida_id}/estado", json={"revisionBase": 0, "estado": cambiado})

    assert conflicto.status_code == 409

    invalido = dict(estado, accionesRestantes=5)
    respuesta = client.put(f"/api/v1/partidas/{partida_id}/estado", json={"revisionBase": 1, "estado": invalido})

    assert respuesta.status_code == 400

    final = dict(estado, estado="FINALIZADA", ronda=8, accionesRestantes=0, ganadores=[ids[0]])
    respuesta = client.put(f"/api/v1/partidas/{partida_id}/estado", json={"revisionBase": 1, "estado": final})

    assert respuesta.status_code == 200
    assert respuesta.json()["revision"] == 2

    estadisticas1 = client.get(f"/api/v1/jugadores/{ids[0]}/estadisticas")
    estadisticas2 = client.get(f"/api/v1/jugadores/{ids[1]}/estadisticas")

    assert estadisticas1.status_code == 200
    assert estadisticas2.status_code == 200
    assert estadisticas1.json()["victorias"] == 1
    assert estadisticas2.json()["partidasFinalizadas"] == 1

    respuesta = client.put(f"/api/v1/partidas/{partida_id}/estado", json={"revisionBase": 2, "estado": cambiado})
    assert respuesta.status_code == 409


def test_validaciones_jugadores(client):
    jugadores = [
        {"nombre": "", "alias": "usuario1"},
        {"nombre": "Ana", "alias": "ab"},
        {"nombre": "Ana", "alias": "alias con espacios"},
        {"nombre": "Ana", "alias": "alias!"}
    ]

    for jugador in jugadores:
        respuesta = client.post("/api/v1/jugadores", json=jugador)
        assert respuesta.status_code == 400
        assert respuesta.json()["codigo"] == "DATOS_INVALIDOS"


def test_validaciones_partidas(client):
    jugador = client.post("/api/v1/jugadores", json={"nombre": "Ana", "alias": "ana_1"})
    jugador_id = jugador.json()["jugadorId"]

    respuesta = client.post("/api/v1/partidas", json={"requestId": "req-002", "jugadores": [jugador_id, jugador_id]})
    assert respuesta.status_code == 400

    respuesta = client.post("/api/v1/partidas", json={"requestId": "req-003", "jugadores": [jugador_id, "J-INEXISTENTE"]})
    assert respuesta.status_code == 400

    respuesta = client.post("/api/v1/partidas", json={"jugadores": [jugador_id]})
    assert respuesta.status_code == 400


def test_generacion_mapa():
    from generador_mapa import generar_partida, validar_tablero

    for indice in range(5):
        matriz, datos = generar_partida(partida_id=f"EX-PRUEBA-{indice}")

        assert matriz is not None
        assert datos is not None
        assert validar_tablero(matriz) is not None

        for simbolo, cantidad in {"C": 1, "X": 1, "T": 4, "P": 4, "S": 15}.items():
            assert sum(casilla == simbolo for fila in datos["matriz"] for casilla in fila) == cantidad

        assert len(datos["casillas"]) == 25
        assert len(datos["secretos_templos"]) == 4
        assert len(datos["pistas"]) == 2
        assert len(datos["pistas"][0]) == 6
        assert len(datos["pistas"][1]) == 4
