package practicaspedagogicas.util;

import practicaspedagogicas.modelo.Usuario;

/**
 * SesionUsuario - Singleton que mantiene el estado de la sesión activa.
 * Almacena el usuario autenticado durante toda la ejecución.
 *
 * Uso:
 *   SesionUsuario.getInstance().setUsuario(usuario);
 *   Usuario u = SesionUsuario.getInstance().getUsuario();
 *
 * @version 1.0
 */
public class SesionUsuario {

    private static SesionUsuario instancia;
    private Usuario usuarioActual;

    private SesionUsuario() {}

    /**
     * Retorna la única instancia de la sesión.
     */
    public static synchronized SesionUsuario getInstance() {
        if (instancia == null) {
            instancia = new SesionUsuario();
        }
        return instancia;
    }

    /** @return Usuario actualmente autenticado en el sistema. */
    public Usuario getUsuario() { return usuarioActual; }

    /** @param usuario Usuario que inició sesión. */
    public void setUsuario(Usuario usuario) { this.usuarioActual = usuario; }

    /** Limpia la sesión (logout). */
    public void cerrarSesion() { this.usuarioActual = null; }

    /** @return true si hay un usuario autenticado. */
    public boolean isAutenticado() { return usuarioActual != null; }

    /**
     * Verifica si el usuario actual tiene uno de los roles permitidos.
     *
     * @param roles Roles permitidos (ej: "Director", "Coordinador").
     * @return true si el usuario tiene alguno de esos roles.
     */
    public boolean tieneRol(String... roles) {
        if (usuarioActual == null) return false;
        for (String rol : roles) {
            if (usuarioActual.getRol().equalsIgnoreCase(rol)) return true;
        }
        return false;
    }
}
