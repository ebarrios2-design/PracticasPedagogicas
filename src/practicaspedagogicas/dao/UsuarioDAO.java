package practicaspedagogicas.dao;

import practicaspedagogicas.modelo.Usuario;
import practicaspedagogicas.util.ConexionDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * UsuarioDAO - CRUD para la tabla USUARIO en Oracle 10g XE.
 *
 * Diferencias respecto a MySQL:
 *  - Secuencia SEQ_USUARIO para IDs
 *  - NUMBER(1) en lugar de BOOLEAN
 *  - SYSDATE en lugar de NOW()
 *  - Sin RETURN_GENERATED_KEYS → usa CURRVAL de la secuencia
 *  - isValid() no disponible en ojdbc14 → se omite
 *
 * @version 1.0 - Oracle 10g XE
 */
public class UsuarioDAO implements DAO<Usuario, Integer> {

    private static final Logger LOG = Logger.getLogger(UsuarioDAO.class.getName());

    // ── SQL Oracle ────────────────────────────────────────────────────────────

    private static final String SQL_INSERTAR =
        "INSERT INTO usuario (id, id_programa, nombres, apellidos, tipo_documento, " +
        "numero_documento, correo, contrasena_hash, rol, telefono) " +
        "VALUES (SEQ_USUARIO.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_ULTIMO_ID =
        "SELECT SEQ_USUARIO.CURRVAL FROM DUAL";

    private static final String SQL_ACTUALIZAR =
        "UPDATE usuario SET id_programa=?, nombres=?, apellidos=?, tipo_documento=?, " +
        "numero_documento=?, correo=?, rol=?, telefono=? WHERE id=?";

    private static final String SQL_CAMBIAR_CONTRASENA =
        "UPDATE usuario SET contrasena_hash=? WHERE id=?";

    private static final String SQL_ELIMINAR =
        "UPDATE usuario SET activo=0 WHERE id=?";

    private static final String SQL_BUSCAR_POR_ID =
        "SELECT u.*, p.nombre AS nombre_programa " +
        "FROM usuario u LEFT JOIN programa p ON u.id_programa = p.id " +
        "WHERE u.id=? AND u.activo=1";

    private static final String SQL_LISTAR =
        "SELECT u.*, p.nombre AS nombre_programa " +
        "FROM usuario u LEFT JOIN programa p ON u.id_programa = p.id " +
        "WHERE u.activo=1 ORDER BY u.apellidos, u.nombres";

    private static final String SQL_LISTAR_POR_ROL =
        "SELECT u.*, p.nombre AS nombre_programa " +
        "FROM usuario u LEFT JOIN programa p ON u.id_programa = p.id " +
        "WHERE u.rol=? AND u.activo=1 ORDER BY u.apellidos, u.nombres";

    private static final String SQL_LISTAR_POR_PROGRAMA_Y_ROL =
        "SELECT u.*, p.nombre AS nombre_programa " +
        "FROM usuario u LEFT JOIN programa p ON u.id_programa = p.id " +
        "WHERE u.id_programa=? AND u.rol=? AND u.activo=1 ORDER BY u.apellidos, u.nombres";

    private static final String SQL_AUTENTICAR =
        "SELECT * FROM usuario WHERE correo=? AND contrasena_hash=? AND activo=1";

    // Oracle: actualizar ultimo_acceso con SYSDATE
    private static final String SQL_ACTUALIZAR_ACCESO =
        "UPDATE usuario SET ultimo_acceso=SYSDATE WHERE id=?";

    private static final String SQL_BUSCAR_POR_CORREO =
        "SELECT u.*, p.nombre AS nombre_programa " +
        "FROM usuario u LEFT JOIN programa p ON u.id_programa = p.id " +
        "WHERE u.correo=? AND u.activo=1";

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @Override
    public boolean insertar(Usuario u) {
        Connection con = null;
        try {
            con = ConexionDB.getConexion();
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(SQL_INSERTAR)) {
                if (u.getIdPrograma() != null)
                    ps.setInt(1, u.getIdPrograma());
                else
                    ps.setNull(1, Types.INTEGER);

                ps.setString(2, u.getNombres());
                ps.setString(3, u.getApellidos());
                ps.setString(4, u.getTipoDocumento());
                ps.setString(5, u.getNumeroDocumento());
                ps.setString(6, u.getCorreo());
                ps.setString(7, u.getContrasenaHash());
                ps.setString(8, u.getRol());
                ps.setString(9, u.getTelefono());

                ps.executeUpdate();

                // Recuperar ID de la secuencia
                try (Statement st = con.createStatement();
                     ResultSet rs = st.executeQuery(SQL_ULTIMO_ID)) {
                    if (rs.next()) u.setId(rs.getInt(1));
                }

                con.commit();
                LOG.info("Usuario insertado: " + u.getCorreo() + " ID=" + u.getId());
                return true;
            }
        } catch (SQLException e) {
            rollback(con);
            LOG.log(Level.SEVERE, "Error insertando usuario: " + e.getMessage(), e);
            return false;
        } finally {
            resetAC(con);
            ConexionDB.liberar(con);
        }
    }

    @Override
    public boolean actualizar(Usuario u) {
        Connection con = null;
        try {
            con = ConexionDB.getConexion();
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(SQL_ACTUALIZAR)) {
                if (u.getIdPrograma() != null)
                    ps.setInt(1, u.getIdPrograma());
                else
                    ps.setNull(1, Types.INTEGER);

                ps.setString(2, u.getNombres());
                ps.setString(3, u.getApellidos());
                ps.setString(4, u.getTipoDocumento());
                ps.setString(5, u.getNumeroDocumento());
                ps.setString(6, u.getCorreo());
                ps.setString(7, u.getRol());
                ps.setString(8, u.getTelefono());
                ps.setInt(9, u.getId());

                int filas = ps.executeUpdate();
                con.commit();
                return filas > 0;
            }
        } catch (SQLException e) {
            rollback(con);
            LOG.log(Level.SEVERE, "Error actualizando usuario: " + e.getMessage(), e);
            return false;
        } finally {
            resetAC(con);
            ConexionDB.liberar(con);
        }
    }

    /** Cambia la contraseña almacenando el nuevo hash SHA-256. */
    public boolean cambiarContrasena(int idUsuario, String nuevoHash) {
        Connection con = null;
        try {
            con = ConexionDB.getConexion();
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(SQL_CAMBIAR_CONTRASENA)) {
                ps.setString(1, nuevoHash);
                ps.setInt(2, idUsuario);
                int f = ps.executeUpdate();
                con.commit();
                return f > 0;
            }
        } catch (SQLException e) {
            rollback(con);
            LOG.log(Level.SEVERE, "Error cambiando contraseña: " + e.getMessage(), e);
            return false;
        } finally {
            resetAC(con);
            ConexionDB.liberar(con);
        }
    }

    @Override
    public boolean eliminar(Integer id) {
        Connection con = null;
        try {
            con = ConexionDB.getConexion();
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(SQL_ELIMINAR)) {
                ps.setInt(1, id);
                int f = ps.executeUpdate();
                con.commit();
                return f > 0;
            }
        } catch (SQLException e) {
            rollback(con);
            return false;
        } finally {
            resetAC(con);
            ConexionDB.liberar(con);
        }
    }

    @Override
    public Usuario buscarPorId(Integer id) {
        Connection con = null;
        try {
            con = ConexionDB.getConexion();
            try (PreparedStatement ps = con.prepareStatement(SQL_BUSCAR_POR_ID)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapear(rs);
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error buscando usuario: " + e.getMessage(), e);
        } finally {
            ConexionDB.liberar(con);
        }
        return null;
    }

    @Override
    public List<Usuario> listarTodos() {
        return listar(SQL_LISTAR);
    }

    /** Lista usuarios filtrados por rol. */
    public List<Usuario> listarPorRol(String rol) {
        List<Usuario> lista = new ArrayList<>();
        Connection con = null;
        try {
            con = ConexionDB.getConexion();
            try (PreparedStatement ps = con.prepareStatement(SQL_LISTAR_POR_ROL)) {
                ps.setString(1, rol);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error listando por rol: " + e.getMessage(), e);
        } finally {
            ConexionDB.liberar(con);
        }
        return lista;
    }

    /** Lista usuarios de un programa con un rol específico. */
    public List<Usuario> listarPorProgramaYRol(int idPrograma, String rol) {
        List<Usuario> lista = new ArrayList<>();
        Connection con = null;
        try {
            con = ConexionDB.getConexion();
            try (PreparedStatement ps = con.prepareStatement(SQL_LISTAR_POR_PROGRAMA_Y_ROL)) {
                ps.setInt(1, idPrograma);
                ps.setString(2, rol);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error listando por programa y rol: " + e.getMessage(), e);
        } finally {
            ConexionDB.liberar(con);
        }
        return lista;
    }

    /**
     * Autentica al usuario verificando correo + hash.
     * Si es exitoso actualiza ultimo_acceso con SYSDATE (Oracle).
     */
    public Usuario autenticar(String correo, String hash) {
        Connection con = null;
        try {
            con = ConexionDB.getConexion();
            try (PreparedStatement ps = con.prepareStatement(SQL_AUTENTICAR)) {
                ps.setString(1, correo);
                ps.setString(2, hash);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Usuario u = mapear(rs);
                        actualizarUltimoAcceso(u.getId());
                        LOG.info("Login exitoso: " + correo + " [" + u.getRol() + "]");
                        return u;
                    }
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error autenticando: " + e.getMessage(), e);
        } finally {
            ConexionDB.liberar(con);
        }
        LOG.warning("Login fallido para: " + correo);
        return null;
    }

    private void actualizarUltimoAcceso(int id) {
        Connection con = null;
        try {
            con = ConexionDB.getConexion();
            try (PreparedStatement ps = con.prepareStatement(SQL_ACTUALIZAR_ACCESO)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Error actualizando último acceso.", e);
        } finally {
            ConexionDB.liberar(con);
        }
    }

    /** Busca usuario por correo electrónico. */
    public Usuario buscarPorCorreo(String correo) {
        Connection con = null;
        try {
            con = ConexionDB.getConexion();
            try (PreparedStatement ps = con.prepareStatement(SQL_BUSCAR_POR_CORREO)) {
                ps.setString(1, correo);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapear(rs);
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error buscando por correo: " + e.getMessage(), e);
        } finally {
            ConexionDB.liberar(con);
        }
        return null;
    }

    // ── Mapeo ─────────────────────────────────────────────────────────────────

    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getInt("id"));

        int idProg = rs.getInt("id_programa");
        u.setIdPrograma(rs.wasNull() ? null : idProg);

        u.setNombres(rs.getString("nombres"));
        u.setApellidos(rs.getString("apellidos"));
        u.setTipoDocumento(rs.getString("tipo_documento"));
        u.setNumeroDocumento(rs.getString("numero_documento"));
        u.setCorreo(rs.getString("correo"));
        u.setContrasenaHash(rs.getString("contrasena_hash"));
        u.setRol(rs.getString("rol"));
        u.setTelefono(rs.getString("telefono"));

        Timestamp fc = rs.getTimestamp("fecha_creacion");
        if (fc != null) u.setFechaCreacion(fc.toLocalDateTime());

        Timestamp ua = rs.getTimestamp("ultimo_acceso");
        if (ua != null) u.setUltimoAcceso(ua.toLocalDateTime());

        // Oracle NUMBER(1)
        u.setActivo(rs.getInt("activo") == 1);
        return u;
    }

    private List<Usuario> listar(String sql) {
        List<Usuario> lista = new ArrayList<>();
        Connection con = null;
        try {
            con = ConexionDB.getConexion();
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error listando usuarios: " + e.getMessage(), e);
        } finally {
            ConexionDB.liberar(con);
        }
        return lista;
    }

    private void rollback(Connection c) {
        if (c != null) try { c.rollback(); } catch (SQLException e) {}
    }
    private void resetAC(Connection c) {
        if (c != null) try { c.setAutoCommit(true); } catch (SQLException e) {}
    }
}
