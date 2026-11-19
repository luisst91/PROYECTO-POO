# La Isla de las Reliquias

Juego de aventuras por turnos para dos jugadores: un cliente de consola en **Java** y un backend en **Python/FastAPI**. El servidor genera un tablero 5x5, reparte reliquias y pistas, cifra la reliquia del cofre y guarda los secretos de cada partida; el cliente Java se conecta a la API para jugar y descubrir el contenido de templos y cofres.

## Descripción del juego

- Tablero de **5x5** con 1 campamento (`C`), 1 cofre (`X`), 4 templos (`T`), 4 pantanos (`P`) y 15 senderos (`S`).
- Partidas de **2 jugadores**, **8 rondas** y **3 acciones por turno**.
- En los templos hay 4 reliquias (valores 1, 2, 2 y 3) y 2 pistas: una de 6 letras y otra de 4 dígitos.
- La reliquia especial del cofre (4 puntos) está cifrada con **AES-256-GCM** y **PBKDF2**; para abrirla se necesita la contraseña formada por las dos pistas: `LETRAS-0000`.
- Gana quien acumule más puntos al entregar reliquias en el campamento. En caso de empate, gana quien haya entregado más reliquias.
- El servidor solo entrega los datos públicos al crear la partida; las reliquias, pistas y el paquete cifrado del cofre se consultan cuando hacen falta.

## Tecnologías

| Parte | Tecnología |
|---|---|
| Backend | Python 3.10+, FastAPI, Uvicorn, Pydantic |
| Cifrado | `cryptography` (AES-GCM + PBKDF2) |
| Mapas | NumPy |
| Cliente | Java 11 o superior (probado con JDK 17) |
| Persistencia | Archivo JSON local |

## Estructura del proyecto

```
backend_isla_reliquias/
├── API/
│   ├── main.py                 # Endpoints de la API
│   ├── generador_mapa.py       # Generación y validación del tablero
│   ├── crypto_service.py       # Cifrado/descifrado del cofre
│   ├── storage.py              # Persistencia en JSON
│   └── conexionPythonJava.java # Cliente de prueba independiente
├── juego/                      # Menú y ciclo principal del juego Java
├── casillas/                   # Tipos de casilla y tablero
├── jugador/                    # Lógica del jugador
├── objetos/                    # Reliquias y pistas
├── SistemaTurnos/              # Turnos y acciones
├── docs/                       # Javadoc generado
├── manual_api.md
├── requirements.txt
└── README.md
```

## Requisitos previos

Comprueba que tienes instalado:

```bash
python --version    # 3.10 o superior
javac -version      # JDK 11 o superior
java -version
```

También necesitas **dos terminales**: una para el servidor Python y otra para el juego Java.

## Puesta en marcha

### 1. Preparar el backend (solo la primera vez)

Desde la raíz del proyecto:

**Windows (PowerShell):**

```powershell
python -m venv .venv
.\.venv\Scripts\Activate.ps1
python -m pip install --upgrade pip
pip install -r requirements.txt
```

**macOS / Linux:**

```bash
python3 -m venv .venv
source .venv/bin/activate
python -m pip install --upgrade pip
pip install -r requirements.txt
```

> Si PowerShell bloquea la activación del entorno virtual, ejecuta una vez:
> `Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass`

### 2. Arrancar la API

En la **terminal 1**, entra en la carpeta `API` y arranca Uvicorn:

```bash
cd API
python -m uvicorn main:app --reload
```

Debe aparecer algo como:

```
Uvicorn running on http://127.0.0.1:8000
```

Comprueba que funciona:

- Panel interactivo: <http://127.0.0.1:8000/docs>
- Estado del servidor: <http://127.0.0.1:8000/salud>
  - Respuesta esperada: `{"disponible":true,"versionContrato":2}`

> Es importante arrancar Uvicorn **desde la carpeta `API`**. Ahí se crea automáticamente la base de datos `API/data/db_expedicion.json` y funcionan los imports internos.

### 3. Compilar el juego Java

En la **terminal 2**, desde la raíz del proyecto (con la API ya encendida):

**Windows (PowerShell):**

```powershell
javac -d bin (Get-ChildItem -Recurse -Filter *.java | Select-Object -ExpandProperty FullName)
```

**macOS / Linux:**

```bash
javac -d bin juego/*.java casillas/*.java jugador/*.java objetos/*.java SistemaTurnos/*.java
```

### 4. Ejecutar el juego

Desde la raíz del proyecto:

```bash
java -cp bin juego.Juego
```

Aparecerá el menú:

```
===== LA ISLA DE LAS RELIQUIAS =====
1. Nueva partida
0. Salir
```

Elige `1`, escribe los nombres de los dos jugadores y juega. La API decide al azar quién empieza.

## Inicio rápido

```bash
# Terminal 1
cd API
pip install -r ../requirements.txt
python -m uvicorn main:app --reload

# Terminal 2 (desde la raíz)
javac -d bin juego/*.java casillas/*.java jugador/*.java objetos/*.java SistemaTurnos/*.java
java -cp bin juego.Juego
```

## Endpoints principales

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/salud` | Comprueba que el servidor está encendido |
| `GET` | `/configuracion` | Reglas y tamaños que usa el cliente Java |
| `POST` | `/partidas` | Crea una partida para 2 jugadores y genera el mapa |
| `GET` | `/partidas` | Lista las partidas guardadas |
| `GET` | `/partidas/{id}` | Devuelve la configuración y el estado de una partida |
| `GET` | `/partidas/{id}/templos/{coord}` | Reliquia y pista de un templo (ej. `C2`) |
| `GET` | `/partidas/{id}/cofre` | Paquete cifrado del cofre |
| `PUT` | `/partidas/{id}/estado` | Guarda un estado completo con control de revisión |
| `POST` | `/jugadores` | Registra un jugador |
| `GET` | `/jugadores` | Lista o filtra jugadores por alias |
| `GET` | `/jugadores/{id}/estadisticas` | Estadísticas acumuladas de un jugador |

Ejemplo para crear una partida desde PowerShell:

```powershell
Invoke-RestMethod -Method Post `
  -Uri http://127.0.0.1:8000/partidas `
  -ContentType "application/json" `
  -Body '{"jugadores":["Ana","Beto"]}'
```

La forma más sencilla de probar los endpoints es abrir <http://127.0.0.1:8000/docs> y usar el botón **Try it out**.

## Persistencia de datos

La API crea y actualiza automáticamente:

```
API/data/db_expedicion.json
```

Ahí se guardan jugadores, partidas, estados, resultados y estadísticas. Ese archivo se escribe primero en un temporal y luego se reemplaza de forma atómica para reducir el riesgo de corrupción.

- Para **reiniciar todo** (borrar jugadores y partidas), detén la API y elimina el archivo o la carpeta `API/data`.
- No borres ese archivo mientras haya una partida en curso: el cliente recibirá errores `404 Partida no encontrada`.
- Si quieres conservar datos, haz una copia de seguridad antes de borrarlo.

## Solución de problemas

### `'python' no se reconoce como un comando...`

Python no está instalado o no está en el `PATH`.

- Instala Python 3.10 o superior desde <https://www.python.org/downloads/> y marca la opción **Add Python to PATH**.
- En Windows también puedes probar con `py -3 --version`.

### `ModuleNotFoundError: No module named 'fastapi'` (o `'cryptography'`, `'numpy'`...)

Las dependencias no están instaladas o el entorno virtual no está activo.

```bash
pip install -r requirements.txt
```

Verifica que el prompt muestre `(.venv)` antes de arrancar la API.

### `'uvicorn' no se reconoce como un comando...`

Usa el módulo de Python en lugar del ejecutable:

```bash
python -m uvicorn main:app --reload
```

### `ModuleNotFoundError: No module named 'storage'` o `'generador_mapa'`

Estás arrancando Uvicorn desde la carpeta equivocada. Entra primero en `API`:

```bash
cd API
python -m uvicorn main:app --reload
```

### `[WinError 10048]` / `address already in use` (puerto 8000 ocupado)

Ya hay otro proceso usando el puerto. Puedes liberarlo o usar otro puerto.

**Windows (PowerShell):**

```powershell
Get-NetTCPConnection -LocalPort 8000 -State Listen | Select-Object OwningProcess
Stop-Process -Id <PID> -Force
```

**macOS / Linux:**

```bash
lsof -i :8000
kill <PID>
```

Si usas otro puerto (`--port 8001`), cambia también `URL_API` en `juego/Juego.java` y vuelve a compilar.

### `Activate.ps1 cannot be loaded because running scripts is disabled`

Permite la ejecución solo para esa terminal:

```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
.\.venv\Scripts\Activate.ps1
```

### `'javac' no se reconoce como un comando...`

Falta el JDK o no está en el `PATH`.

- Instala un **JDK** (no solo el JRE), por ejemplo Temurin 17: <https://adoptium.net/>.
- Cierra y vuelve a abrir la terminal.
- Comprueba con `javac -version`.

### `package casillas does not exist` / `cannot find symbol` al compilar

Se está compilando desde una carpeta interna o no se incluyeron todos los paquetes.

Compila **desde la raíz del proyecto** y borra la salida anterior si es necesario:

```powershell
Remove-Item -Recurse -Force bin -ErrorAction SilentlyContinue
javac -d bin (Get-ChildItem -Recurse -Filter *.java | Select-Object -ExpandProperty FullName)
```

No compiles únicamente `juego/Juego.java` desde dentro de `juego/`, porque el resto de paquetes no se encontrarían.

### `NO SE PUDO CONECTAR CON EL SERVIDOR. ESTA ENCENDIDO?`

El cliente Java no encuentra la API.

1. Comprueba que la terminal 1 siga ejecutando Uvicorn sin errores.
2. Abre <http://127.0.0.1:8000/salud> en el navegador.
3. Confirma que `Juego.URL_API` sea `http://127.0.0.1:8000` (o el puerto que estés usando).
4. Si cambiaste el puerto, vuelve a compilar el cliente.

### `ERROR DEL SERVIDOR: 404` / `Partida no encontrada`

La partida ya no existe en la base de datos. Suele ocurrir si se borró `API/data/db_expedicion.json` o si se reinició el servidor sin ese archivo.

- Vuelve a crear una partida desde el menú (`1. Nueva partida`).
- No borres la base de datos durante una partida.

### `ERROR DEL SERVIDOR: 409` / `La revision actual es ...`

Se intentó guardar un estado basado en una revisión antigua. Ocurre al usar dos clientes sobre la misma partida o al reutilizar un estado viejo.

- Usa un solo cliente por partida.
- Descarta el guardado y vuelve a leer el estado actual con `GET /partidas/{id}` antes de guardar.
- Para empezar de cero, crea una partida nueva.

### `ERROR DEL SERVIDOR: 500` / `No se pudo generar un mapa valido`

El generador descartó todos los mapas candidatos. Es poco frecuente, pero puede pasar tras cambiar las reglas del tablero o las cantidades de casillas.

- Reintenta crear la partida.
- Revisa que el total de `CANTIDAD_POR_TIPO` en `API/generador_mapa.py` coincida con el tamaño del tablero (25 casillas para 5x5).
- Si modificaste las validaciones, comprueba que exista al menos un templo alcanzable y una pareja de templos con pistas válidas.

### `json.decoder.JSONDecodeError` al arrancar la API

El archivo `API/data/db_expedicion.json` está dañado o vacío.

- Restaura una copia de seguridad si la tienes.
- Si no necesitas los datos, detén la API, elimina `API/data/db_expedicion.json` y vuelve a arrancar: se creará uno nuevo.

### Los acentos o símbolos se ven mal en la consola

La consola no está usando UTF-8.

**Windows:**

```powershell
chcp 65001
```

También puedes ejecutar Java con:

```bash
java -Dfile.encoding=UTF-8 -cp bin juego.Juego
```

### `pip install requirements.txt` no instala nada o da error

Ese comando trata `requirements.txt` como un nombre de paquete. La forma correcta es:

```bash
pip install -r requirements.txt
```

## Cambiar las reglas del juego

- Las reglas generales (rondas, acciones por turno, capacidad y costos) están en `CONFIGURACION` dentro de `API/main.py`.
- El tamaño y la composición del tablero están en `API/generador_mapa.py` (`CANTIDAD_FILAS`, `CANTIDAD_COLUMNAS` y `CANTIDAD_POR_TIPO`).
- El cliente Java pide la configuración con `GET /configuracion` antes de cada partida, así que no hace falta cambiar esos valores en Java.
- Si cambias las dimensiones del tablero, ajusta también la composición de casillas para que el total siga siendo `filas × columnas`.

## Documentación adicional

- `manual_api.md`: guía rápida para crear una partida desde el panel `/docs`.
- `docs/index.html`: Javadoc generado de las clases Java; ábrelo en el navegador.

## Autores

Ernesto Gutiérrez, Diego Nieto y Luis Torres.
