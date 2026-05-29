package practicaspedagogicas.dao;

import practicaspedagogicas.modelo.Programa;
import practicaspedagogicas.util.ConexionDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ProgramaDAO - CRUD para la tabla PROGRAMA en Oracle 10g XE.
 *
 * Cambios respecto a MySQL:
 *  - INSERT usa RETURNING id INTO ? en lugar de RETURN_GENERATED_KEYS
 *  - Secuencia SEQ_PROGRAMA genera los IDs automáticamente
 *  - NUMBER(1) en lugar de BOOLEAN
 *  - SYSDATE en lugar de CURRENT_TIMESTAMP
 *  - VARCHAR2 en lugar de VARCHAR
 *
 * @version 1.0 - Oracle 10g XE
 */
public class ProgramaDAO implements DAO<Programa, Integer> {

    private static final Logger LOG = Logger.getLogger(ProgramaDAO.class.getName());

    // ── SQL Oracle ────────────────────────────────────────────────────────────
    // Oracle 10g: usamos SEQ_PROGRAMA.NEXTVAL para el ID
    private static final String SQL_INSERTAR =
        "INSERT INTO programa (id, nombre, codigo_snies, facultad, modalidad, nivel, acreditado) " +
        "VALUES (SEQ_PROGRAMA.NEXTVAL, ?, ?, ?, ?, ?, ?)";

    // Para recuperar el ID recién insertado en Oracle
    private static final String SQL_ULTIMO_ID =
        "SELECT SEQ_PROGRAMA.CURRVAL FROM DUAL";

    private static final String SQL_ACTUALIZAR =
        "UPDATE programa SET nombre=?, codigo_snies=?, facultad=?, " +
        "modalidad=?, nivel=?, acreditado=? WHERE id=?";

    // Eliminación lógica: activo = 0
    private static final String SQL_ELIMINAR =
        "UPDATE programa SET activo = 0 WHERE id = ?";

    private static final String SQL_BUSCAR_POR_ID =
        "SELECT * FROM programa WHERE id = ? AND activo = 1";

    private static final String SQL_LISTAR =
        "SELECT * FROM programa WHERE activo = 1 ORDER BY nombre";

    private static final String SQL_BUSCAR_POR_NOMBRE =
        "SELECT * FROM programa WHERE UPPER(nombre) LIKE UPPER(?) AND activo = 1 ORDER BY nombre";

    private static final String SQL_CONTAR =
        "SELECT COUNT(*) FROM programa WHERE activo = 1";

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @Override
    public boolean insertar(Programa p) {
        Connection con = null;
        try {
            con = ConexionDB.getConexion();
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(SQL_INSERTAR)) {
                ps.setString(1, p.getNombre());
                ps.setString(2, p.getCodigoSnies());
                ps.setString(3, p.getFacultad());
                ps.setString(4, p.getModalidad());
                ps.setString(5, p.getNivel());
                // Oracle NUMBER(1): 1 = true, 0 = false
                ps.setInt(6, p.isAcreditado() ? 1 : 0);

                ps.executeUpdate();

                // Recuperar ID generado por la secuencia
                try (Statement st = con.createStatement();
                     ResultSet rs = st.executeQuery(SQL_ULTIMO_ID)) {
                    if (rs.next()) p.setId(rs.getInt(1));
                }

                con.commit();
                LOG.info("Programa insertado con ID: " + p.getId());
                return true;
            }
        } catch (SQLException e) {
            rollback(con);
            LOG.log(Level.SEVERE, "Error insertando programa: " + e.getMessage(), e);
            return false;
        } finally {
            resetAC(con);
            ConexionDB.liberar(con);
        }
    }

    @Override
    public boolean actualizar(Programa p) {
        Connection con = null;
        try {
            con = ConexionDB.getConexion();
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(SQL_ACTUALIZAR)) {
                ps.setString(1, p.getNombre());
                ps.setString(2, p.getCodigoSnies());
                ps.setString(3, p.getFacultad());
                ps.setString(4, p.getModalidad());
                ps.setString(5, p.getNivel());
                ps.setInt(6, p.isAcreditado() ? 1 : 0);
                ps.setInt(7, p.getId());

                int filas = ps.executeUpdate();
                con.commit();
                return filas > 0;
            }
        } catch (SQLException e) {
            rollback(con);
            LOG.log(Level.SEVERE, "Error actualizando programa: " + e.getMessage(), e);
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
                int filas = ps.executeUpdate();
                con.commit();
                return filas > 0;
            }
        } catch (SQLException e) {
            rollback(con);
            LOG.log(Level.SEVERE, "Error eliminando programa: " + e.getMessage(), e);
            return false;
        } finally {
            resetAC(con);
            ConexionDB.liberar(con);
        }
    }

    @Override
    public Programa buscarPorId(Integer id) {
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
            LOG.log(Level.SEVERE, "Error buscando programa: " + e.getMessage(), e);
        } finally {
            ConexionDB.liberar(con);
        }
        return null;
    }

    @Override
    public List<Programa> listarTodos() {
        List<Programa> lista = new ArrayList<>();
        Connection con = null;
        try {
            con = ConexionDB.getConexion();
            try (PreparedStatement ps = con.prepareStatement(SQL_LISTAR);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error listando programas: " + e.getMessage(), e);
        } finally {
            ConexionDB.liberar(con);
        }
        return lista;
    }

    /** Búsqueda parcial por nombre (case-insensitive gracias a UPPER). */
    public List<Programa> buscarPorNombre(String texto) {
        List<Programa> lista = new ArrayList<>();
        Connection con = null;
        try {
            con = ConexionDB.getConexion();
            try (PreparedStatement ps = con.prepareStatement(SQL_BUSCAR_POR_NOMBRE)) {
                ps.setString(1, "%" + texto + "%");
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error buscando por nombre: " + e.getMessage(), e);
        } finally {
            ConexionDB.liberar(con);
        }
        return lista;
    }

    /** Cuenta programas activos. */
    public int contar() {
        Connection con = null;
        try {
            con = ConexionDB.getConexion();
            try (PreparedStatement ps = con.prepareStatement(SQL_CONTAR);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error contando programas: " + e.getMessage(), e);
        } finally {
            ConexionDB.liberar(con);
        }
        return 0;
    }

    // ── Mapeo ResultSet → Programa ────────────────────────────────────────────

    private Programa mapear(ResultSet rs) throws SQLException {
        Programa p = new Programa();
        p.setId(rs.getInt("id"));
        p.setNombre(rs.getString("nombre"));
        p.setCodigoSnies(rs.getString("codigo_snies"));
        p.setFacultad(rs.getString("facultad"));
        p.setModalidad(rs.getString("modalidad"));
        p.setNivel(rs.getString("nivel"));
        // Oracle NUMBER(1): 1 = true
        p.setAcreditado(rs.getInt("acreditado") == 1);
        Timestamp ts = rs.getTimestamp("fecha_registro");
        if (ts != null) p.setFechaRegistro(ts.toLocalDateTime());
        p.setActivo(rs.getInt("activo") == 1);
        return p;
    }

    private void rollback(Connection c) {
        if (c != null) try { c.rollback(); } catch (SQLException e) {}
    }
    private void resetAC(Connection c) {
        if (c != null) try { c.setAutoCommit(true); } catch (SQLException e) {}
    }
}
