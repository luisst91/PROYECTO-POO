# Manual Rápido API - Isla de las Reliquias

## 1. Iniciar el Servidor
Abre tu terminal en la carpeta del proyecto y ejecuta:
```bash
uvicorn main:app --reload
```

## 2. Abrir el Panel de Control
Entra en tu navegador a: **http://127.0.0.1:8000/docs**

## 3. Crear una Partida
1. Despliega la ruta verde **`POST /partidas`**.
2. Haz clic en **"Try it out"**.
3. Pega este código en la caja de texto:
```json
{
  "jugadores": ["alias1", "alias2"]
}
```
4. Presiona el botón azul **"Execute"**.

## Resultado
La API generará el mapa con el cofre cifrado y guardará todo automáticamente en el archivo `data/db_expedicion.json`.