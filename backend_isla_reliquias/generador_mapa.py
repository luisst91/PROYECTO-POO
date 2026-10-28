import numpy as np
from colorama import Back, Fore, Style, init
from collections import deque
import random
import string
from crypto_service import CryptoService

CANTIDAD_FILAS = 5
CANTIDAD_COLUMNAS = 5
MAXIMO_CANDIDATOS = 1000

CANTIDAD_POR_TIPO = {"C": 1, "X": 1, "T": 4, "P": 4, "S": 15}

NOMBRE_POR_TIPO = {
    "C": "Campamento",
    "S": "Sendero",
    "P": "Pantano",
    "T": "Templo",
    "X": "Cofre"
}

COLOR_POR_TIPO = {
    "C": Back.GREEN,
    "S": Back.WHITE,
    "P": Back.MAGENTA,
    "T": Back.CYAN,
    "X": Back.YELLOW
}


def convertir_a_coordenada(fila, columna):
    letra_columna = chr(ord("A") + columna)
    numero_fila = fila + 1
    return letra_columna + str(numero_fila)


def convertir_a_indices(coordenada):
    columna = ord(coordenada[0].upper()) - ord("A")
    fila = int(coordenada[1:]) - 1
    return fila, columna


def obtener_vecinos(fila, columna):
    lista_vecinos = []
    cambios_fila = [-1, 1, 0, 0]
    cambios_columna = [0, 0, -1, 1]

    for indice in range(4):
        fila_vecina = fila + cambios_fila[indice]
        columna_vecina = columna + cambios_columna[indice]

        if fila_vecina >= 0 and fila_vecina < CANTIDAD_FILAS:
            if columna_vecina >= 0 and columna_vecina < CANTIDAD_COLUMNAS:
                lista_vecinos.append((fila_vecina, columna_vecina))

    return lista_vecinos


def buscar_posiciones(matriz_tablero, tipo_casilla):
    lista_posiciones = []

    for fila in range(CANTIDAD_FILAS):
        for columna in range(CANTIDAD_COLUMNAS):
            if matriz_tablero[fila][columna] == tipo_casilla:
                lista_posiciones.append((fila, columna))

    return lista_posiciones

"""
holisss, la funcion "distancia_sin_pantanos"  calcula el camino más
corto en numero de casillas entre la posicion inicial y el destino, limitando a que:

1 - solo se pueden hacer movimientos de arriba, abajo, derecha e izquierda.
2 - no se puede pisar ningun pantano, en este caso el pantano es "P"

internamente:
*cola: es la lista donde los datos se atienden en orden de llegada, guarda las casillas 
pendientes por revisar con su distancia actual(fila, columna, distancia)

*visitados: es el registro donde se guardan las casillas por las que ya se pasó para no volver otra vez

*obtener_vecinos: da las 4 casillas adyacentes (arriba, abajo, izq, derecha)


"""

def distancia_sin_pantanos(matriz_tablero, origen, destino):
    if origen == destino:
        return 0

    visitados = {origen}
    cola = deque([(origen[0], origen[1], 0)])

    while cola:
        fila_actual, col_actual, distancia = cola.popleft()

        for f_vecina, c_vecina in obtener_vecinos(fila_actual, col_actual):
            if(f_vecina, c_vecina) not in visitados and matriz_tablero[f_vecina][c_vecina] != "P":
                if(f_vecina, c_vecina) == destino:
                    return distancia + 1
                visitados.add((f_vecina, c_vecina))
                cola.append((f_vecina, c_vecina, distancia + 1))
    return None

"""
holisss, al validar_tablero de ernesto le agregue esto para que cumpla mas las reglas del juego:

* una funcion que calcula la distancia mas corta caminando normal y por los pantanos no se puede pasar
* ahora no acepta un mapa si no tiene al menos un templo a 2 pasos o menos de la base
* el cofre no aparece tracado entre pantanos o en esquinas
* de los 4 templos que hay el codigo escoge 2 random que cumplan con la ruta para esconder las pistas
* se calcula la vuelta completa, si ese trayecto toma mas de 12 pasos el mapa se descarta porque el juego tiene 8 rondas

"""


def validar_tablero(matriz_tablero):
    posicion_campamento = buscar_posiciones(matriz_tablero, "C")[0]
    posicion_cofre = buscar_posiciones(matriz_tablero, "X")[0]
    posiciones_templos = buscar_posiciones(matriz_tablero, "T")

    vecinos_libres = 0
    for posicion_vecina in obtener_vecinos(posicion_campamento[0], posicion_campamento[1]):
        if matriz_tablero[posicion_vecina[0]][posicion_vecina[1]] != "P":
            vecinos_libres = vecinos_libres + 1

    if vecinos_libres < 2:
        return None

    distancias_c_templos = {}
    templo_cercano = False
    for pos_t in posiciones_templos:
        dist = distancia_sin_pantanos(matriz_tablero, posicion_campamento, pos_t)
        if dist is not None:
            distancias_c_templos[pos_t] = dist
            if dist <= 2:
                templo_cercano = True

    if not templo_cercano:
        return None

    dist_c_x = distancia_sin_pantanos(matriz_tablero, posicion_campamento, posicion_cofre)
    if dist_c_x is None:
        return None

    parejas_validas = []
    for i in range(len(posiciones_templos)):
        for j in range(len(posiciones_templos)):
            if i == j:
                continue
            t1 = posiciones_templos[i]
            t2 = posiciones_templos[j]

            if t1 not in distancias_c_templos or t2 not in distancias_c_templos:
                continue

            dist_t1_t2 = distancia_sin_pantanos(matriz_tablero, t1, t2)
            if dist_t1_t2 is None:
                continue

            ruta1 = distancias_c_templos[t1] + dist_t1_t2 + distancias_c_templos[t2] + (2 * dist_c_x)
            ruta2 = distancias_c_templos[t2] + dist_t1_t2 + distancias_c_templos[t1] + (2 * dist_c_x)

            if min(ruta1, ruta2) <= 12:
                parejas_validas.append((t1, t2))

    if not parejas_validas:
        return None

    t_pista1, t_pista2 = random.choice(parejas_validas)

    datos_partida = {
        "campamento": convertir_a_coordenada(posicion_campamento[0], posicion_campamento[1]),
        "cofre": convertir_a_coordenada(posicion_cofre[0], posicion_cofre[1]),
        "templos": [convertir_a_coordenada(t[0], t[1]) for t in posiciones_templos],
        "templo_pista_1": convertir_a_coordenada(t_pista1[0], t_pista1[1]),
        "templo_pista_2": convertir_a_coordenada(t_pista2[0], t_pista2[1])
    }

    return datos_partida


def generar_matriz_aleatoria():
    lista_casillas = []

    for tipo_casilla in CANTIDAD_POR_TIPO:
        for repeticion in range(CANTIDAD_POR_TIPO[tipo_casilla]):
            lista_casillas.append(tipo_casilla)

    lista_casillas = np.random.permutation(lista_casillas)
    matriz_tablero = lista_casillas.reshape(CANTIDAD_FILAS, CANTIDAD_COLUMNAS)

    return matriz_tablero

"""
holisss, a generar_partida le agregue esto para armar el juego completo con las reglas del pdf:
* ahora genera dos pistas: la pista 1 con 6 letras y la pista 2 con 4 digitos que se unen con un guion para la clave
* reparte al azar las 4 reliquias normales con sus valores exactos de 1, 2, 2 y 3 puntos entre los 4 templos
* esconde las dos pistas en los dos templos que cumplieron la ruta y deja los otros dos solo con su reliquia
* cifra la reliquia especial de 4 puntos en el cofre con AES-GCM y comprueba que se pueda descifrar con la clave antes de aceptar el mapa
* si juegan los mismos dos jugadores en una partida nueva, no deja repetir el campamento, el cofre ni las mismas pistas
* convierte la matriz de numpy a una lista de casillas con sus coordenadas de A1 a E5 para que la api pueda enviarla en json sin errores
"""


def generar_partida(semilla=None, campamento_anterior=None, cofre_anterior=None, pistas_anteriores=None, partida_id="EX-RED-01"):
    if semilla is not None:
        np.random.seed(semilla)
        random.seed(semilla)

    for numero_intento in range(1, MAXIMO_CANDIDATOS + 1):
        matriz_tablero = generar_matriz_aleatoria()
        datos_partida = validar_tablero(matriz_tablero)

        if datos_partida is None:
            continue

        if campamento_anterior is not None and datos_partida["campamento"] == campamento_anterior:
            continue

        if cofre_anterior is not None and datos_partida["cofre"] == cofre_anterior:
            continue

        pista1 = "".join(random.choices(string.ascii_uppercase, k=6))
        pista2 = f"{random.randint(0, 9999):04d}"

        if pistas_anteriores is not None and (pista1, pista2) == tuple(pistas_anteriores):
            continue

        password = f"{pista1}-{pista2}"

        paquete_cofre = CryptoService.encrypt_chest_payload(
            partida_id=partida_id,
            cofre_id="COFRE-01",
            reliquia_id="REL-ESPECIAL",
            valor=4,
            password=password
        )

        if not CryptoService.verify_chest_payload(paquete_cofre, password):
            continue

        valores_reliquias = [1, 2, 2, 3]
        random.shuffle(valores_reliquias)

        secretos_templos = {}
        for idx, coord_t in enumerate(datos_partida["templos"]):
            pista_obj = None
            if coord_t == datos_partida["templo_pista_1"]:
                pista_obj = {"indice": 1, "fragmento": pista1}
            elif coord_t == datos_partida["templo_pista_2"]:
                pista_obj = {"indice": 2, "fragmento": pista2}

            secretos_templos[coord_t] = {
                "coordenada": coord_t,
                "reliquia": {"reliquiaId": f"REL-0{idx + 1}", "valor": valores_reliquias[idx]},
                "pista": pista_obj
            }

        matriz_lista = matriz_tablero.tolist()
        casillas_lista = []
        for f in range(CANTIDAD_FILAS):
            for c in range(CANTIDAD_COLUMNAS):
                casillas_lista.append({
                    "coordenada": convertir_a_coordenada(f, c),
                    "fila": f,
                    "columna": c,
                    "tipo": matriz_lista[f][c]
                })

        datos_partida["matriz"] = matriz_lista
        datos_partida["casillas"] = casillas_lista
        datos_partida["secretos_templos"] = secretos_templos
        datos_partida["paquete_cofre"] = paquete_cofre
        datos_partida["pistas"] = [pista1, pista2]

        return matriz_tablero, datos_partida

    return None, None



def crear_jugador(nombre, posicion_inicial, color_texto, color_fondo):
    jugador = {
        "nombre": nombre,
        "posicion": posicion_inicial,
        "color_texto": color_texto,
        "color_fondo": color_fondo
    }
    return jugador


def crear_jugadores(datos_partida):
    posicion_campamento = convertir_a_indices(datos_partida["campamento"])
    primer_jugador = crear_jugador("Jugador 1", posicion_campamento, Fore.RED, Back.RED)
    segundo_jugador = crear_jugador("Jugador 2", posicion_campamento, Fore.BLUE, Back.BLUE)
    return [primer_jugador, segundo_jugador]


def obtener_posicion_jugador(jugador):
    return jugador["posicion"]


def obtener_coordenada_jugador(jugador):
    posicion = obtener_posicion_jugador(jugador)
    return convertir_a_coordenada(posicion[0], posicion[1])


def obtener_tipo_casilla_jugador(matriz_tablero, jugador):
    posicion = obtener_posicion_jugador(jugador)
    tipo_casilla = matriz_tablero[posicion[0]][posicion[1]]
    return NOMBRE_POR_TIPO[tipo_casilla]


def obtener_jugadores_en_casilla(lista_jugadores, fila, columna):
    jugadores_encontrados = []

    for jugador in lista_jugadores:
        posicion = obtener_posicion_jugador(jugador)
        if posicion[0] == fila and posicion[1] == columna:
            jugadores_encontrados.append(jugador)

    return jugadores_encontrados


def mostrar_ubicacion_jugador(matriz_tablero, jugador):
    coordenada = obtener_coordenada_jugador(jugador)
    nombre_casilla = obtener_tipo_casilla_jugador(matriz_tablero, jugador)
    print(jugador["color_texto"] + jugador["nombre"] + Style.RESET_ALL, "esta en", coordenada, "(" + nombre_casilla + ")")


def mostrar_ubicacion_todos_los_jugadores(matriz_tablero, lista_jugadores):
    for jugador in lista_jugadores:
        mostrar_ubicacion_jugador(matriz_tablero, jugador)


#esto de imprimir tablero maybe lo borremos

"""
def imprimir_tablero(matriz_tablero, lista_jugadores):
    print()
    encabezado = "       "
    for columna in range(CANTIDAD_COLUMNAS):
        encabezado = encabezado + chr(ord("A") + columna) + "      "
    print(encabezado)

    for fila in range(CANTIDAD_FILAS):
        linea = "  " + str(fila + 1) + "   "

        for columna in range(CANTIDAD_COLUMNAS):
            tipo_casilla = matriz_tablero[fila][columna]
            jugadores_en_casilla = obtener_jugadores_en_casilla(lista_jugadores, fila, columna)

            color_casilla = COLOR_POR_TIPO[tipo_casilla]
            linea = linea + color_casilla + Fore.BLACK + " " + tipo_casilla + " "

            for jugador in jugadores_en_casilla:
                linea = linea + jugador["color_texto"] + Style.BRIGHT + "●" + Style.NORMAL + Fore.BLACK

            for relleno in range(2 - len(jugadores_en_casilla)):
                linea = linea + " "

            linea = linea + " " + Style.RESET_ALL + " "

        print(linea)
        print()
"""


if __name__ == "__main__":
    matriz_resultado, datos_resultado = generar_partida()
    jugadores_partida = crear_jugadores(datos_resultado)
    #imprimir_tablero(matriz_resultado, jugadores_partida)