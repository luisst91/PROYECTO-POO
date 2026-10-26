import os
import json
import tempfile

"""
* configura la carpeta y el archivo donde se va a guardar toda la informacion del juego (db_expedicion.json)
* tiene una base para que sepa ccmo arrancar la primera vez que lo corres
* la funcion cargar_db revisa si el archivo ya existe para leerlo, y si no existe lo crea desde 0
* en vez de guardar directo, se crea un archivo temporal y escribe todo ahí primero
* cuando ya termino de escribir, reemplaza ese archivo temporal por el original de un solo golpe, así si pasa x cosa, el archivo
tienes menos posibilidad de corromperse
"""


DATA_DIR = "data"
DB_FILE = os.path.join(DATA_DIR, "db_expedicion.json")

DB_INICIAL = {
    "jugadores": {},
    "partidas": {},
    "requests_partidas": {}
}

def cargar_db():
    if not os.path.exists(DB_FILE):
        os.makedirs(DATA_DIR, exist_ok=True)
        guardar_db(DB_INICIAL)
        return DB_INICIAL

    with open(DB_FILE, "r", encoding="utf-8") as f:
        return json.load(f)

def guardar_db(datos):
    os.makedirs(DATA_DIR, exist_ok=True)
    fd, ruta_temporal = tempfile.mkstemp(dir=DATA_DIR, prefix="tmp_", suffix=".json")
    try:
        with os.fdopen(fd, "w", encoding="utf-8") as f_temp:
            json.dump(datos, f_temp, ensure_ascii=False, indent=2)
            f_temp.flush()
            os.fsync(f_temp.fileno())
        os.replace(ruta_temporal, DB_FILE)
    except Exception as e:
        if os.path.exists(ruta_temporal):
            os.remove(ruta_temporal)
        raise e