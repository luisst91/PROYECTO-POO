package juego;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;


/**
 * Utilidad criptográfica encargada de descifrar el contenido del cofe usando las librerias
 * estándar de seguridad de Java ({@code javax.crypto})
 * Emplea derivación de claves mediante PBKF2 con HMAC-SHA256 y descifrando autenticado AES-GCM
 * 
 * @author Ernesto Gutiérrez, Diego Nieto, Luis Torres
 * @version 1.0
 * 
 */

public class Descifrador {


    /**
     * Descifra un paquete de datos en formato JSON usando una clave secreta proporcionada por el jugador
     * Extrae los parámetros criptograficos codificados en Base64 ({@code saltBase64}, {@code nonceBase64}),
     * {@code aadBase64}, {@code cifradoConTagBase64} y el numero de {@code iteraciones} mediante {@link ClienteApi#valorDe(String, String)}
     * 
     * 
     * @param paquete Cadena en formato JSON que contiene los metadatos criptograficos y el mensaje cifrado del cofre
     * @param clave Contraseña secreta  introducida para derivar la llave simétrica AES de 256 bits
     * @return El mensaje original descifrado en codificación UTF-8, o {@code null} si la clave es incorrecta, la verificacion de autenticidad falla o el paquete JSON está corrupto
     */
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
