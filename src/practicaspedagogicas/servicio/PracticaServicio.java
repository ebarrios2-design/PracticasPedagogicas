package practicaspedagogicas.servicio;

import practicaspedagogicas.dao.PracticaDAO;
import practicaspedagogicas.modelo.Practica;

import java.util.List;
import java.util.logging.Logger;

/**
 * PracticaServicio - Lógica de negocio para la gestión de prácticas pedagógicas.
 * Aplica validaciones de negocio antes de delegar al DAO.
 *
 * @version 1.0
 */
public class PracticaServicio {

    private static final Logger LOG = Logger.getLogger(PracticaServicio.class.getName());
    private final PracticaDAO dao = new PracticaDAO();

    /**
     * Crea una nueva práctica validando reglas de negocio:
     *  - Máximo 8 prácticas por programa.
     *  - El número de práctica (1-8) no puede estar duplicado.
     *  - Las horas mínimas deben ser positivas.
     *  - La fecha de fin no puede ser anterior a la de inicio.
     *
     * @param practica Objeto Practica a persistir.
     * @return Resultado con éxito/error y mensaje.
     */
    public Resultado<Practica> crear(Practica practica) {
        // Validar horas
        if (practica.getHorasMinimas() <= 0)
            return Resultado.error("Las horas mínimas deben ser un valor positivo.");

        // Validar fechas
        if (practica.getFechaFin() != null && practica.getFechaInicio() != null
                && practica.getFechaFin().isBefore(practica.getFechaInicio()))
            return Resultado.error("La fecha de fin no puede ser anterior a la fecha de inicio.");

        // Validar límite de 8 prácticas
        int total = dao.contarPorPrograma(practica.getIdPrograma());
        if (total >= 8)
            return Resultado.error("El programa ya tiene el máximo de 8 prácticas registradas.");

        boolean ok = dao.insertar(practica);
        return ok ? Resultado.exito(practica, "Práctica creada exitosamente.")
                  : Resultado.error("No se pudo guardar la práctica. Verifique que el número no esté duplicado.");
    }

    /**
     * Actualiza una práctica existente.
     */
    public Resultado<Practica> actualizar(Practica practica) {
        if (practica.getId() <= 0)
            return Resultado.error("ID de práctica inválido.");

        if (practica.getHorasMinimas() <= 0)
            return Resultado.error("Las horas mínimas deben ser un valor positivo.");

        boolean ok = dao.actualizar(practica);
        return ok ? Resultado.exito(practica, "Práctica actualizada.")
                  : Resultado.error("No se pudo actualizar la práctica.");
    }

    /**
     * Desactiva (elimina lógicamente) una práctica.
     * No se permite si la práctica tiene grupos con estudiantes activos.
     */
    public Resultado<Void> eliminar(int idPractica) {
        boolean ok = dao.eliminar(idPractica);
        return ok ? Resultado.exito(null, "Práctica desactivada.")
                  : Resultado.error("No se pudo desactivar la práctica.");
    }

    /** Retorna todas las prácticas de un programa ordenadas por número. */
    public List<Practica> listarPorPrograma(int idPrograma) {
        return dao.listarPorPrograma(idPrograma);
    }

    /** Retorna todas las prácticas activas del sistema. */
    public List<Practica> listarTodas() {
        return dao.listarTodos();
    }

    /** Busca una práctica por ID. */
    public Practica buscarPorId(int id) {
        return dao.buscarPorId(id);
    }

    /**
     * Cambia el estado de una práctica siguiendo el flujo:
     * Planificada → Activa → Finalizada
     */
    public Resultado<Void> cambiarEstado(int idPractica, String nuevoEstado) {
        Practica p = dao.buscarPorId(idPractica);
        if (p == null) return Resultado.error("Práctica no encontrada.");

        // Validar transición de estado
        String actual = p.getEstado();
        boolean transicionValida =
            ("Planificada".equals(actual) && "Activa".equals(nuevoEstado)) ||
            ("Activa".equals(actual)      && "Finalizada".equals(nuevoEstado)) ||
            ("Activa".equals(actual)      && "Cancelada".equals(nuevoEstado));

        if (!transicionValida)
            return Resultado.error("Transición de estado no válida: " + actual + " → " + nuevoEstado);

        boolean ok = dao.cambiarEstado(idPractica, nuevoEstado);
        return ok ? Resultado.exito(null, "Estado actualizado a: " + nuevoEstado)
                  : Resultado.error("No se pudo cambiar el estado.");
    }

    // ── Clase interna Resultado ───────────────────────────────────────────────

    /**
     * Encapsula el resultado de una operación de servicio.
     * @param <T> Tipo del dato retornado.
     */
    public static class Resultado<T> {
        private final boolean exito;
        private final String  mensaje;
        private final T       dato;

        private Resultado(boolean exito, T dato, String mensaje) {
            this.exito   = exito;
            this.dato    = dato;
            this.mensaje = mensaje;
        }

        public static <T> Resultado<T> exito(T dato, String mensaje) {
            return new Resultado<>(true, dato, mensaje);
        }

        public static <T> Resultado<T> error(String mensaje) {
            return new Resultado<>(false, null, mensaje);
        }

        public boolean isExito()  { return exito; }
        public String  getMensaje(){ return mensaje; }
        public T       getDato()  { return dato; }
    }
}
