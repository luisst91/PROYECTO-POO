import os
import json
import tempfile
import copy
from pathlib import Path

"""
* configura la carpeta y el archivo donde se va a guardar toda la informacion del juego (db_expedicion.json)
* tiene una base para que sepa ccmo arrancar la primera vez que lo corres
* la funcion cargar_db revisa si el archivo ya existe para leerlo, y si no existe lo crea desde 0
* en vez de guardar directo, se crea un archivo temporal y escribe todo ahí primero
* cuando ya termino de escribir, reemplaza ese archivo temporal por el original de un solo golpe, así si pasa x cosa, el archivo
tienes menos posibilidad de corromperse
"""

DATA_DIR = Path(
    os.environ.get(
        "ISLA_DATA_DIR",
        str(Path(__file__).resolve().parent / "data")
    )
)

DB_FILE = DATA_DIR / "db_expedicion.json"

DB_INICIAL = {
    "jugadores": {},
    "partidas": {},
    "requests_partidas": {}
}

def cargar_db():
    if not DB_FILE.exists():
        DATA_DIR.mkdir(parents=True, exist_ok=True)
        guardar_db(copy.deepcopy(DB_INICIAL))
        return copy.deepcopy(DB_INICIAL)

    with DB_FILE.open("r", encoding="utf-8") as archivo:
        datos = json.load(archivo)

    for clave in DB_INICIAL:
        datos.setdefault(clave, {})

    return datos

def guardar_db(datos):
    DATA_DIR.mkdir(parents=True, exist_ok=True)

    fd, ruta_temporal = tempfile.mkstemp(
        dir=DATA_DIR,
        prefix="tmp_",
        suffix=".json"
    )

    try:
        with os.fdopen(fd, "w", encoding="utf-8") as archivo:
            json.dump(
                datos,
                archivo,
                ensure_ascii=False,
                indent=2
            )
            archivo.flush()
            os.fsync(archivo.fileno())

        os.replace(ruta_temporal, DB_FILE)

    finally:
        if os.path.exists(ruta_temporal):
            os.remove(ruta_temporal)
