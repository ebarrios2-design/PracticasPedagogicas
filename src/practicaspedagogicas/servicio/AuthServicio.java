package practicaspedagogicas.servicio;

import practicaspedagogicas.dao.UsuarioDAO;
import practicaspedagogicas.modelo.Usuario;
import practicaspedagogicas.util.HashUtil;
import practicaspedagogicas.util.SesionUsuario;

import java.util.logging.Logger;

/**
 * AuthServicio - Lógica de negocio para autenticación de usuarios.
 * Intermediario entre la vista de login y el UsuarioDAO.
 *
 * @version 1.0
 */
public class AuthServicio {

    private static final Logger LOG = Logger.getLogger(AuthServicio.class.getName());
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    /**
     * Intenta autenticar al usuario con correo y contraseña en texto plano.
     * Si es exitoso, establece la sesión activa.
     *
     * @param correo     Correo institucional del usuario.
     * @param contrasena Contraseña en texto plano.
     * @return Usuario autenticado o null si las credenciales son incorrectas.
     */
    public Usuario login(String correo, String contrasena) {
        if (correo == null || correo.isBlank() ||
            contrasena == null || contrasena.isBlank()) {
            LOG.warning("Intento de login con credenciales vacías.");
            return null;
        }

        String hash = HashUtil.sha256(contrasena);
        if (hash == null) return null;

        Usuario usuario = usuarioDAO.autenticar(correo.trim().toLowerCase(), hash);

        if (usuario != null) {
            SesionUsuario.getInstance().setUsuario(usuario);
            LOG.info("Sesión iniciada: " + usuario.getNombreCompleto() + " [" + usuario.getRol() + "]");
        }
        return usuario;
    }

    /**
     * Cierra la sesión del usuario actual.
     */
    public void logout() {
        Usuario u = SesionUsuario.getInstance().getUsuario();
        if (u != null) {
            LOG.info("Sesión cerrada: " + u.getNombreCompleto());
        }
        SesionUsuario.getInstance().cerrarSesion();
    }

    /**
     * Cambia la contraseña del usuario autenticado.
     *
     * @param idUsuario     ID del usuario.
     * @param contrasenaActual Contraseña actual (texto plano).
     * @param contrasenaNueva  Nueva contraseña (texto plano).
     * @return true si el cambio fue exitoso.
     */
    public boolean cambiarContrasena(int idUsuario, String contrasenaActual, String contrasenaNueva) {
        if (contrasenaNueva == null || contrasenaNueva.length() < 6) {
            LOG.warning("La nueva contraseña debe tener al menos 6 caracteres.");
            return false;
        }

        Usuario u = usuarioDAO.buscarPorId(idUsuario);
        if (u == null) return false;

        // Verificar que la contraseña actual sea correcta
        if (!HashUtil.verificar(contrasenaActual, u.getContrasenaHash())) {
            LOG.warning("Contraseña actual incorrecta para usuario ID: " + idUsuario);
            return false;
        }

        String nuevoHash = HashUtil.sha256(contrasenaNueva);
        return usuarioDAO.cambiarContrasena(idUsuario, nuevoHash);
    }
}
