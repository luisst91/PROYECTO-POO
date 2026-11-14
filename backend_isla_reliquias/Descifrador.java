package juego;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

// Descifra el cofre con las librerias estandar de Java: PBKDF2 (clave) + AES-GCM (cifrado)
public class Descifrador {

    // devuelve el texto descifrado, o null si la contrasena es incorrecta o el paquete esta danado
    public static String descifrar(String paquete, String clave) {
        try {
            byte[] sal = Base64.getDecoder().decode(ClienteApi.valorDe(paquete, "saltBase64"));
            byte[] nonce = Base64.getDecoder().decode(ClienteApi.valorDe(paquete, "nonceBase64"));
            byte[] aad = Base64.getDecoder().decode(ClienteApi.valorDe(paquete, "aadBase64"));
            byte[] cifrado = Base64.getDecoder().decode(ClienteApi.valorDe(paquete, "cifradoConTagBase64"));
            int iteraciones = Integer.parseInt(ClienteApi.valorDe(paquete, "iteraciones"));

            PBEKeySpec especificacion = new PBEKeySpec(clave.toCharArray(), sal, iteraciones, 256);
            byte[] bytesClave = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(especificacion).getEncoded();

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(bytesClave, "AES"), new GCMParameterSpec(128, nonce));
            cipher.updateAAD(aad);
            byte[] texto = cipher.doFinal(cifrado);

            return new String(texto, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }
}
