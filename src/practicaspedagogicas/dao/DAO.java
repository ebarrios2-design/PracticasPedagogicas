package practicaspedagogicas.dao;

import java.util.List;

/**
 * Interfaz genérica DAO con operaciones CRUD estándar.
 * Todas las clases DAO del sistema implementan esta interfaz.
 *
 * @param <T>  Tipo de entidad (Programa, Usuario, Practica, etc.)
 * @param <ID> Tipo del identificador (generalmente Integer)
 *
 * @version 1.0
 */
public interface DAO<T, ID> {

    /**
     * Inserta un nuevo registro en la base de datos.
     * @param entidad Objeto a persistir.
     * @return true si la inserción fue exitosa.
     */
    boolean insertar(T entidad);

    /**
     * Actualiza un registro existente en la base de datos.
     * @param entidad Objeto con datos actualizados (debe tener ID válido).
     * @return true si la actualización afectó al menos una fila.
     */
    boolean actualizar(T entidad);

    /**
     * Elimina lógicamente un registro (activo = FALSE).
     * Nunca elimina físicamente para garantizar trazabilidad.
     * @param id Identificador del registro a desactivar.
     * @return true si se desactivó correctamente.
     */
    boolean eliminar(ID id);

    /**
     * Busca un registro por su ID.
     * @param id Identificador del registro.
     * @return Objeto encontrado o null si no existe.
     */
    T buscarPorId(ID id);

    /**
     * Retorna todos los registros activos.
     * @return Lista de objetos activos.
     */
    List<T> listarTodos();
}
