import os
import json
import base64
import hashlib
from cryptography.hazmat.primitives.ciphers.aead import AESGCM


class CryptoService:
    ITERATIONS = 100_000
    KEY_LENGTH = 32  # 256 bits

    @staticmethod
    def derive_key(password: str, salt_bytes: bytes) -> bytes:
        """Deriva una clave de 256 bits usando PBKDF2WithHmacSHA256."""
        return hashlib.pbkdf2_hmac(
            hash_name="sha256",
            password=password.encode("utf-8"),
            salt=salt_bytes,
            iterations=CryptoService.ITERATIONS,
            dklen=CryptoService.KEY_LENGTH
        )

    @staticmethod
    def encrypt_chest_payload(
        partida_id: str,
        cofre_id: str,
        reliquia_id: str,
        valor: int,
        password: str,
        salt_bytes: bytes = None,
        nonce_bytes: bytes = None
    ) -> dict:
        """Genera el paquete cifrado en formato Base64 para ser consumido por la API/Java."""
        if salt_bytes is None:
            salt_bytes = os.urandom(16)
        if nonce_bytes is None:
            nonce_bytes = os.urandom(12)

        key = CryptoService.derive_key(password, salt_bytes)

        payload = json.dumps(
            {
                "partidaId": partida_id,
                "cofreId": cofre_id,
                "reliquiaId": reliquia_id,
                "valor": valor
            },
            separators=(",", ":")
        ).encode("utf-8")

        aad = f"{partida_id}|{cofre_id}".encode("utf-8")

        aesgcm = AESGCM(key)
        ciphertext_with_tag = aesgcm.encrypt(nonce_bytes, payload, aad)

        return {
            "partidaId": partida_id,
            "cofreId": cofre_id,
            "algoritmo": "AES/GCM/NoPadding",
            "kdf": "PBKDF2WithHmacSHA256",
            "iteraciones": CryptoService.ITERATIONS,
            "longitudClaveBits": CryptoService.KEY_LENGTH * 8,
            "saltBase64": base64.b64encode(salt_bytes).decode("utf-8"),
            "nonceBase64": base64.b64encode(nonce_bytes).decode("utf-8"),
            "tagBits": 128,
            "aadBase64": base64.b64encode(aad).decode("utf-8"),
            "cifradoConTagBase64": base64.b64encode(ciphertext_with_tag).decode("utf-8")
        }

    @staticmethod
    def verify_chest_payload(paquete: dict, password: str) -> bool:
        """Comprueba que el paquete generado se descifra correctamente con su contraseña."""
        try:
            salt = base64.b64decode(paquete["saltBase64"])
            nonce = base64.b64decode(paquete["nonceBase64"])
            aad = base64.b64decode(paquete["aadBase64"])
            ciphertext = base64.b64decode(paquete["cifradoConTagBase64"])

            key = CryptoService.derive_key(password, salt)
            aesgcm = AESGCM(key)
            decrypted = aesgcm.decrypt(nonce, ciphertext, aad)
            datos = json.loads(decrypted.decode("utf-8"))

            return (
                datos.get("partidaId") == paquete["partidaId"]
                and datos.get("cofreId") == paquete["cofreId"]
                and datos.get("valor") == 4
            )
        except Exception:
            return False