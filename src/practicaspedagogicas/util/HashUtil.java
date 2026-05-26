package practicaspedagogicas.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utilidad para cifrado de contraseñas con SHA-256.
 * Las contraseñas NUNCA se almacenan en texto plano.
 *
 * @version 1.0
 */
public class HashUtil {

    private static final Logger LOG = Logger.getLogger(HashUtil.class.getName());

    private HashUtil() {}

    /**
     * Genera el hash SHA-256 de un texto dado.
     *
     * @param texto Texto a cifrar (contraseña en plano).
     * @return String con el hash hexadecimal de 64 caracteres, o null si hay error.
     */
    public static String sha256(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(texto.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error generando hash SHA-256.", e);
            return null;
        }
    }

    /**
     * Verifica si una contraseña en plano coincide con un hash almacenado.
     *
     * @param textoPlano   Contraseña ingresada por el usuario.
     * @param hashAlmacenado Hash SHA-256 guardado en la base de datos.
     * @return true si coinciden.
     */
    public static boolean verificar(String textoPlano, String hashAlmacenado) {
        String hashIngresado = sha256(textoPlano);
        return hashIngresado != null && hashIngresado.equals(hashAlmacenado);
    }
}
