# Manual de la API — Expedición Isla de las Reliquias

## Introducción

La API de Expedición Isla de las Reliquias es el componente del proyecto encargado de administrar la información de los jugadores, generar los escenarios de las partidas y mantener su estado mediante un sistema de persistencia.

Está desarrollada en Python utilizando FastAPI y funciona como un servicio HTTP que puede ser consumido por la aplicación principal desarrollada en Java. Esta separación permite que Java gestione la interacción con los jugadores y las reglas del juego, mientras que Python proporciona los datos necesarios para iniciar, consultar y recuperar las partidas.

La API también incorpora mecanismos de cifrado para proteger el contenido del cofre, validaciones sobre los datos recibidos y un sistema de revisiones que evita sobrescribir accidentalmente el estado más reciente de una partida.

## 1. Requisitos previos

Para ejecutar el servicio es necesario contar con:

- Python 3.10 o superior.
- Un administrador de paquetes compatible con `pip`.
- Las dependencias especificadas en `requirements.txt`.
- Acceso a una terminal para iniciar el servidor.

Se recomienda utilizar un entorno virtual de Python para mantener las dependencias del proyecto aisladas de otras instalaciones.

## 2. Instalación y configuración

Desde una terminal, ubíquese en el directorio `backend_isla_reliquias`, donde se encuentra el archivo `requirements.txt`.

Instale las dependencias mediante el siguiente comando:

```bash
python -m pip install -r requirements.txt
```

Una vez completada la instalación, acceda al directorio `API`:

```bash
cd API
```

Inicie el servidor de desarrollo:

```bash
python -m uvicorn main:app --reload
```

Por defecto, el servicio estará disponible en:

```text
http://127.0.0.1:8000
```

El parámetro `--reload` permite reiniciar automáticamente el servidor cuando se detectan modificaciones en el código. Está pensado para el desarrollo y las pruebas locales.

Para detener el servidor, presione `Ctrl + C` en la terminal.

## 3. Documentación interactiva

FastAPI genera automáticamente una interfaz de documentación basada en OpenAPI. Esta permite consultar las rutas disponibles, revisar los modelos de entrada y salida y enviar solicitudes HTTP directamente desde el navegador.

La documentación Swagger UI se encuentra en:

```text
http://127.0.0.1:8000/docs
```

También está disponible una representación alternativa mediante ReDoc:

```text
http://127.0.0.1:8000/redoc
```

Para realizar una solicitud desde Swagger UI, seleccione el endpoint correspondiente, presione `Try it out`, complete los parámetros necesarios y utilice `Execute`.

La interfaz mostrará la respuesta del servidor, su código de estado HTTP y los datos devueltos.

## 4. Organización del servicio

La implementación de la API se distribuye entre varios archivos, cada uno con una responsabilidad específica.

| Archivo | Responsabilidad |
|---|---|
| `main.py` | Define los endpoints HTTP, procesa solicitudes y aplica las validaciones del contrato. |
| `generador_mapa.py` | Genera tableros, comprueba las condiciones del escenario y distribuye los elementos de cada partida. |
| `crypto_service.py` | Gestiona el cifrado y la verificación del contenido protegido del cofre. |
| `storage.py` | Administra la lectura y escritura de los datos persistentes en formato JSON. |
| `conexionPythonJava.java` | Proporciona un ejemplo de comunicación HTTP desde Java hacia la API. |

El servicio utiliza el prefijo `/api/v1` para sus endpoints y expone la versión 2 del contrato de comunicación.

## 5. Endpoints disponibles

Los endpoints permiten administrar jugadores, crear partidas, consultar escenarios y almacenar el progreso del juego.

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/v1/salud` | Comprueba la disponibilidad del servicio. |
| POST | `/api/v1/jugadores` | Registra un nuevo jugador. |
| GET | `/api/v1/jugadores` | Consulta los jugadores registrados. |
| GET | `/api/v1/jugadores/{jugador_id}/estadisticas` | Obtiene las estadísticas de un jugador. |
| POST | `/api/v1/partidas` | Genera una partida para dos jugadores registrados. |
| GET | `/api/v1/partidas` | Consulta las partidas almacenadas. |
| GET | `/api/v1/partidas/{partida_id}` | Recupera la configuración y el estado de una partida. |
| GET | `/api/v1/partidas/{partida_id}/templos/{coord}` | Consulta el contenido de un templo. |
| GET | `/api/v1/partidas/{partida_id}/cofre` | Obtiene el paquete cifrado del cofre. |
| PUT | `/api/v1/partidas/{partida_id}/estado` | Actualiza el estado de una partida. |

Los identificadores incluidos entre llaves representan parámetros que deben sustituirse por valores reales.

## 6. Gestión de jugadores

Antes de generar una partida es necesario registrar a los dos jugadores que participarán en ella.

### 6.1. Registrar un jugador

**Endpoint:** `POST /api/v1/jugadores`

Ejemplo de solicitud:

```json
{
  "nombre": "Ana González",
  "alias": "ana_01"
}
```

El nombre debe contener entre 1 y 80 caracteres. El alias debe tener entre 3 y 20 caracteres y solamente puede incluir letras, números, guiones y guiones bajos.

Los alias son únicos y su comparación no distingue entre mayúsculas y minúsculas. Por ejemplo, `ana_01` y `ANA_01` se consideran el mismo alias.

Ejemplo de respuesta:

```json
{
  "jugadorId": "J-A1B2C3D4E5F6",
  "nombre": "Ana González",
  "alias": "ana_01"
}
```

El campo `jugadorId` identifica al jugador dentro del sistema y debe conservarse para las solicitudes posteriores.

### 6.2. Consultar jugadores

**Endpoint:** `GET /api/v1/jugadores`

Devuelve los jugadores registrados en el sistema. También admite el parámetro opcional `alias` para realizar una búsqueda específica.

Ejemplo:

```text
GET /api/v1/jugadores?alias=ana_01
```

### 6.3. Consultar estadísticas

**Endpoint:** `GET /api/v1/jugadores/{jugador_id}/estadisticas`

Permite consultar la cantidad de partidas finalizadas, victorias, puntos acumulados y reliquias entregadas por un jugador.

Las estadísticas se calculan a partir de las partidas que se encuentran en estado `FINALIZADA`. Las partidas en curso no se incluyen en estos resultados.

## 7. Creación de partidas

**Endpoint:** `POST /api/v1/partidas`

Para generar una partida se deben proporcionar los identificadores de dos jugadores previamente registrados y un identificador de solicitud denominado `requestId`.

Ejemplo:

```json
{
  "requestId": "solicitud-001",
  "jugadores": [
    "J-A1B2C3D4E5F6",
    "J-112233445566"
  ]
}
```

Los identificadores mostrados son ilustrativos y deben reemplazarse por los valores obtenidos al registrar a los jugadores.

Al recibir una solicitud válida, la API realiza las siguientes operaciones:

1. Comprueba que ambos jugadores existan y sean diferentes.
2. Verifica si el identificador de solicitud ya fue utilizado.
3. Genera un tablero aleatorio de 5 × 5 casillas.
4. Valida las condiciones de distribución y accesibilidad del escenario.
5. Distribuye las reliquias y los fragmentos de la contraseña entre los templos.
6. Genera el contenido cifrado del cofre.
7. Determina aleatoriamente el orden inicial de los jugadores.
8. Almacena la configuración, los secretos y el estado inicial de la partida.

La respuesta incluye el identificador de la partida, la configuración pública, el estado inicial y el número de revisión.

Los secretos de los templos y el contenido cifrado del cofre no se incluyen en esta respuesta.

### 7.1. Composición del tablero

Cada escenario contiene 25 casillas, distribuidas de la siguiente manera:

| Símbolo | Tipo | Cantidad |
|---|---|---:|
| C | Campamento | 1 |
| X | Cofre | 1 |
| T | Templo | 4 |
| P | Pantano | 4 |
| S | Sendero | 15 |

Las coordenadas utilizan letras de `A` a `E` para las columnas y números del `1` al `5` para las filas.

El generador evalúa los escenarios candidatos antes de aceptarlos. Entre sus verificaciones se encuentran la accesibilidad de los puntos relevantes y las restricciones de distancia establecidas para el recorrido.

Si no consigue generar un escenario válido dentro del límite de intentos, la API devuelve un error indicando que la generación no pudo completarse.

### 7.2. Idempotencia de las solicitudes

El campo `requestId` permite identificar una operación de creación.

Cuando se repite una solicitud con el mismo `requestId` y los mismos jugadores, el servicio devuelve la partida previamente creada en lugar de generar otra.

Si se reutiliza el mismo identificador con una lista diferente de jugadores, la solicitud se rechaza con un error de conflicto.

Este comportamiento permite recuperar el resultado de una operación cuando el cliente no recibe la respuesta inicial, por ejemplo, debido a una interrupción de la comunicación.

## 8. Consulta de partidas

### 8.1. Listar partidas

**Endpoint:** `GET /api/v1/partidas`

Devuelve un resumen de las partidas almacenadas, incluyendo su identificador, jugadores, estado y ronda actual.

Es posible filtrar los resultados mediante el parámetro opcional `jugadorId`.

### 8.2. Recuperar una partida

**Endpoint:** `GET /api/v1/partidas/{partida_id}`

Permite recuperar la configuración pública y el estado persistente de una partida.

Esta operación resulta especialmente útil cuando la aplicación Java necesita reanudar una sesión previamente guardada.

La respuesta incluye el número de revisión actual, que debe utilizarse como referencia al enviar una actualización del estado.

## 9. Consulta de templos y cofre

Los elementos especiales del escenario se consultan mediante endpoints independientes.

### 9.1. Contenido de los templos

**Endpoint:** `GET /api/v1/partidas/{partida_id}/templos/{coord}`

Devuelve la información asociada a un templo específico, incluyendo su coordenada, la reliquia correspondiente y, cuando exista, un fragmento de la contraseña del cofre.

Ejemplo:

```text
GET /api/v1/partidas/EX-A1B2C3D4E5F6/templos/B3
```

La coordenada debe corresponder a un templo real del escenario. En caso contrario, el servicio devuelve un error `404`.

### 9.2. Contenido cifrado del cofre

**Endpoint:** `GET /api/v1/partidas/{partida_id}/cofre`

Devuelve el paquete criptográfico asociado al cofre de una partida.

La contraseña se construye utilizando dos fragmentos distribuidos entre los templos: uno compuesto por seis letras y otro por cuatro dígitos.

El contenido del cofre se protege mediante AES-GCM y una clave derivada de la contraseña utilizando PBKDF2-HMAC-SHA256.

La API entrega el paquete cifrado, pero no proporciona directamente la contraseña necesaria para descifrarlo. El proceso de recuperación de pistas y apertura del cofre corresponde a la lógica de la aplicación Java.

## 10. Persistencia del estado

**Endpoint:** `PUT /api/v1/partidas/{partida_id}/estado`

El servicio permite guardar el progreso de una partida mediante un snapshot que representa su estado en un momento determinado.

La solicitud contiene dos elementos principales:

- `revisionBase`: número de revisión sobre el cual se realizó la actualización.
- `estado`: estructura con la información actual de la partida.

El estado incluye, entre otros datos, la ronda actual, el turno, las acciones restantes, las posiciones de los jugadores, sus inventarios, las reliquias entregadas y los elementos explorados.

### 10.1. Control de revisiones

Cada partida comienza con la revisión `0`.

Cuando se guarda correctamente un nuevo estado, la API incrementa el número de revisión y lo devuelve al cliente.

Por ejemplo, si una partida se encuentra en la revisión `3`, la siguiente actualización debe indicar:

```json
{
  "revisionBase": 3,
  "estado": {
    "...": "Estado completo de la partida"
  }
}
```

El ejemplo anterior es ilustrativo: el objeto `estado` debe contener todos los campos exigidos por el contrato.

Si otro proceso ya guardó una revisión más reciente, el servidor rechaza la actualización con un error `409`.

De esta manera, se evita que un estado antiguo sobrescriba accidentalmente el progreso más reciente.

### 10.2. Reintentos de guardado

Cuando se recibe nuevamente una solicitud idéntica al último guardado exitoso, la API devuelve la revisión existente sin incrementarla.

Esto permite manejar reintentos de comunicación sin registrar dos veces la misma actualización.

### 10.3. Finalización de partidas

El estado puede pasar de `EN_CURSO` a `FINALIZADA` cuando se cumplen las condiciones estructurales requeridas.

Una vez finalizada la partida, el servicio impide registrar nuevas modificaciones. Sus resultados permanecen disponibles para las consultas y el cálculo de estadísticas.

La validación de las reglas de movimiento, exploración, entrega de reliquias y resolución de turnos corresponde a la aplicación Java.

## 11. Almacenamiento de datos

La persistencia se implementa mediante el módulo `storage.py`, que utiliza un archivo JSON como almacenamiento local.

De forma predeterminada, los datos se guardan en:

```text
API/data/db_expedicion.json
```

El archivo contiene tres colecciones principales:

| Colección | Contenido |
|---|---|
| `jugadores` | Información de los jugadores registrados. |
| `partidas` | Configuraciones, estados y secretos de las partidas. |
| `requests_partidas` | Identificadores de solicitudes utilizados para evitar creaciones duplicadas. |

El directorio de almacenamiento se crea automáticamente cuando es necesario guardar información.

La ruta también puede configurarse mediante la variable de entorno `ISLA_DATA_DIR`.

Para reducir el riesgo de dejar archivos incompletos durante una escritura, el módulo utiliza un archivo temporal y lo reemplaza por el archivo de almacenamiento una vez completada la operación.

Este mecanismo está orientado al uso local del proyecto. El bloqueo utilizado por la API coordina las operaciones dentro de un mismo proceso Python, pero no sustituye un sistema de concurrencia entre múltiples procesos o servidores.

## 12. Manejo de errores

La API devuelve respuestas HTTP con un código que indica el resultado de cada operación.

Los errores definidos por el contrato utilizan una estructura uniforme:

```json
{
  "codigo": "PARTIDA_NO_ENCONTRADA",
  "mensaje": "La partida no existe"
}
```

Los principales códigos HTTP utilizados son:

| Código | Significado |
|---|---|
| 200 | Solicitud procesada correctamente. |
| 201 | Recurso creado correctamente. |
| 400 | Datos de entrada inválidos o estructura incorrecta. |
| 404 | Recurso solicitado inexistente. |
| 409 | Conflicto con un registro o revisión existente. |
| 503 | No fue posible completar la generación del escenario. |

Los mensajes de error permiten identificar la causa del problema y facilitan su tratamiento desde el cliente Java.

## 13. Pruebas del servicio

El proyecto incluye pruebas automatizadas y una colección de Postman para comprobar el funcionamiento de los endpoints.

### 13.1. Pruebas automatizadas

Desde el directorio `backend_isla_reliquias`, ejecute:

```bash
python -m pytest tests/test_api.py -v
```

Las pruebas están diseñadas para verificar el registro de jugadores, la creación de partidas, las consultas, las validaciones de entrada, el control de revisiones y la generación de escenarios.

También comprueban algunos comportamientos importantes, como la reutilización de `requestId` y la prevención de guardados duplicados.

### 13.2. Pruebas con Postman

La colección se encuentra en:

```text
API/Isla_Reliquias.postman_collection.json
```

Para utilizarla:

1. Inicie el servidor de FastAPI.
2. Abra Postman y seleccione la opción `Import`.
3. Importe el archivo de la colección.
4. Ejecute las solicitudes de registro de jugadores.
5. Cree una partida utilizando los identificadores obtenidos.
6. Consulte el tablero, los templos, el cofre y el estado persistente.

La colección incorpora variables que facilitan la reutilización de identificadores entre solicitudes.

Algunas operaciones requieren ajustar los datos al escenario generado. Por ejemplo, la consulta de un templo debe utilizar una coordenada válida y el guardado del estado debe respetar el orden y las posiciones reales de los jugadores.

## 14. Integración con Java

La comunicación entre Java y Python se realiza mediante solicitudes HTTP que intercambian información en formato JSON.

La aplicación Java actúa como cliente y FastAPI como servidor. Por lo tanto, ambos componentes pueden desarrollarse y ejecutarse de manera independiente.

El flujo general de integración consiste en registrar a los jugadores, solicitar la creación de una partida, recuperar su configuración y administrar el desarrollo del juego desde Java.

A medida que avanza la partida, Java puede consultar los elementos especiales del escenario y enviar actualizaciones del estado para conservar el progreso.

El archivo `conexionPythonJava.java` contiene un ejemplo básico de conexión con el servicio y de procesamiento de la respuesta correspondiente a la creación de una partida.

Para utilizarlo, es necesario que el servidor esté en ejecución y que los identificadores enviados correspondan a jugadores registrados.

## Consideraciones finales

La API proporciona una interfaz centralizada para administrar los datos necesarios durante una partida de Expedición Isla de las Reliquias.

Su organización separa la generación de escenarios, la protección de información, la persistencia y la exposición de endpoints HTTP. Esto permite mantener responsabilidades diferenciadas entre el backend desarrollado en Python y la aplicación principal desarrollada en Java.

La documentación interactiva, las pruebas automatizadas y la colección de Postman facilitan la comprobación del servicio y permiten verificar su comportamiento durante el desarrollo y la integración del proyecto.
