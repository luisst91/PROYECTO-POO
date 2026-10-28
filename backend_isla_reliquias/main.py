from fastapi import FastAPI
from pydantic import BaseModel
from typing import List
import uuid

from storage import cargar_db, guardar_db
from generador_mapa import generar_partida

"""
HOLISSS

* usa FastAPI para crear la ruta "POST /partidas" que conecta por donde el programa en Java va a pedir crear los juegos
* recibe la lista de alias de los jugadores y le inventa un ID unico a esa partida (tipo PARTIDA-A1B2C3D4)
* llama a tu función de generador_mapa.py para que fabrique un tablero nuevo en el momento
* guarda todo el mapa, las coordenadas, el cofre cifrado y las pistas en un archivo json
* manda esos datos ordenados a storage.py para que se guarden
* por ultimo le devuelve todo el mapa y los datos listos al cliente para que puedan empezar a jugar
"""

app = FastAPI(title="API Isla de las Reliquias")

class CrearPartidaRequest(BaseModel):
    jugadores: List[str]

@app.post("/partidas")
def crear_partida(request: CrearPartidaRequest):
    db = cargar_db()
    
    partida_id = f"PARTIDA-{str(uuid.uuid4())[:8].upper()}"
    
    matriz, datos_partida = generar_partida(partida_id=partida_id)
    
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
    db["partidas"][partida_id] = nueva_partida
    guardar_db(db)
    
    return {"mensaje": "Partida generada con éxito", "partida": nueva_partida}