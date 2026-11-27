package agenda;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class GestorCifrado {
    
    private static final String CLAVE_SECRETA = "ClaveSecreta1234";
    private static final SecretKeySpec llave = new SecretKeySpec(CLAVE_SECRETA.getBytes(StandardCharsets.UTF_8), "AES");

    public static String cifrarAES(String textoPlano) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, llave);
            byte[] bytesCifrados = cipher.doFinal(textoPlano.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(bytesCifrados);
        } catch (Exception e) {
            return "ERROR_CIFRADO";
        }
    }

    public static String descifrarAES(String textoCifradoBase64) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, llave);
            byte[] bytesDecodificados = Base64.getDecoder().decode(textoCifradoBase64);
            byte[] bytesDescifrados = cipher.doFinal(bytesDecodificados);
            return new String(bytesDescifrados, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "[Error al descifrar]";
        }
    }
}
