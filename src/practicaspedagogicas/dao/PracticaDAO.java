package practicaspedagogicas.dao;

import practicaspedagogicas.modelo.Practica;
import practicaspedagogicas.util.ConexionDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * PracticaDAO - CRUD para la tabla PRACTICA en Oracle 10g XE.
 *
 * Diferencias respecto a MySQL:
 *  - Secuencia SEQ_PRACTICA para IDs
 *  - NUMBER(1) en lugar de BOOLEAN
 *  - TO_DATE / DATE nativo de Oracle para fechas
 *  - SYSDATE en lugar de CURRENT_TIMESTAMP
 *
 * @version 1.0 - Oracle 10g XE
 */
public class PracticaDAO implements DAO<Practica, Integer> {

    private static final Logger LOG = Logger.getLogger(PracticaDAO.class.getName());

    // ── SQL Oracle ────────────────────────────────────────────────────────────

    private static final String SQL_INSERTAR =
        "INSERT INTO practica (id, id_programa, numero, nombre, tipo, objetivos, " +
        "horas_minimas, semestre, fecha_inicio, fecha_fin, estado) " +
        "VALUES (SEQ_PRACTICA.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_ULTIMO_ID =
        "SELECT SEQ_PRACTICA.CURRVAL FROM DUAL";

    private static final String SQL_ACTUALIZAR =
        "UPDATE practica SET nombre=?, tipo=?, objetivos=?, horas_minimas=?, " +
        "semestre=?, fecha_inicio=?, fecha_fin=?, estado=? WHERE id=?";

    private static final String SQL_ELIMINAR =
        "UPDATE practica SET activo=0 WHERE id=?";

    private static final String SQL_BUSCAR_POR_ID =
        "SELECT pr.*, p.nombre AS nombre_programa " +
        "FROM practica pr JOIN programa p ON pr.id_programa = p.id " +
        "WHERE pr.id=? AND pr.activo=1";

    private static final String SQL_LISTAR =
        "SELECT pr.*, p.nombre AS nombre_programa " +
        "FROM practica pr JOIN programa p ON pr.id_programa = p.id " +
        "WHERE pr.activo=1 ORDER BY pr.id_programa, pr.numero";

    private static final String SQL_LISTAR_POR_PROGRAMA =
        "SELECT pr.*, p.nombre AS nombre_programa " +
        "FROM practica pr JOIN programa p ON pr.id_programa = p.id " +
        "WHERE pr.id_programa=? AND pr.activo=1 ORDER BY pr.numero";

    private static final String SQL_CONTAR_POR_PROGRAMA =
        "SELECT COUNT(*) FROM practica WHERE id_programa=? AND activo=1";

    private static final String SQL_CAMBIAR_ESTADO =
        "UPDATE practica SET estado=? WHERE id=?";

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @Override
    public boolean insertar(Practica pr) {
        if (contarPorPrograma(pr.getIdPrograma()) >= 8) {
            LOG.warning("El programa ya tiene 8 prácticas registradas.");
            return false;
        }

        Connection con = null;
        try {
            con = ConexionDB.getConexion();
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(SQL_INSERTAR)) {
                ps.setInt(1, pr.getIdPrograma());
                ps.setInt(2, pr.getNumero());
                ps.setString(3, pr.getNombre());
                ps.setString(4, pr.getTipo());
                ps.setString(5, pr.getObjetivos());
                ps.setInt(6, pr.getHorasMinimas());
                ps.setString(7, pr.getSemestre());
                // Oracle acepta java.sql.Date directamente
                ps.setDate(8, pr.getFechaInicio() != null
                    ? Date.valueOf(pr.getFechaInicio()) : null);
                ps.setDate(9, pr.getFechaFin() != null
                    ? Date.valueOf(pr.getFechaFin()) : null);
                ps.setString(10, pr.getEstado() != null ? pr.getEstado() : "Planificada");

                ps.executeUpdate();

                try (Statement st = con.createStatement();
                     ResultSet rs = st.executeQuery(SQL_ULTIMO_ID)) {
                    if (rs.next()) pr.setId(rs.getInt(1));
                }

                con.commit();
                LOG.info("Práctica insertada ID: " + pr.getId());
                return true;
            }
        } catch (SQLException e) {
            rollback(con);
            LOG.log(Level.SEVERE, "Error insertando práctica: " + e.getMessage(), e);
            return false;
        } finally {
            resetAC(con);
            ConexionDB.liberar(con);
        }
    }

    @Override
    public boolean actualizar(Practica pr) {
        Connection con = null;
        try {
            con = ConexionDB.getConexion();
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(SQL_ACTUALIZAR)) {
                ps.setString(1, pr.getNombre());
                ps.setString(2, pr.getTipo());
                ps.setString(3, pr.getObjetivos());
                ps.setInt(4, pr.getHorasMinimas());
                ps.setString(5, pr.getSemestre());
                ps.setDate(6, pr.getFechaInicio() != null
                    ? Date.valueOf(pr.getFechaInicio()) : null);
                ps.setDate(7, pr.getFechaFin() != null
                    ? Date.valueOf(pr.getFechaFin()) : null);
                ps.setString(8, pr.getEstado());
                ps.setInt(9, pr.getId());

                int filas = ps.executeUpdate();
                con.commit();
                return filas > 0;
            }
        } catch (SQLException e) {
            rollback(con);
            LOG.log(Level.SEVERE, "Error actualizando práctica: " + e.getMessage(), e);
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
    public Practica buscarPorId(Integer id) {
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
            LOG.log(Level.SEVERE, "Error buscando práctica: " + e.getMessage(), e);
        } finally {
            ConexionDB.liberar(con);
        }
        return null;
    }

    @Override
    public List<Practica> listarTodos() {
        List<Practica> lista = new ArrayList<>();
        Connection con = null;
        try {
            con = ConexionDB.getConexion();
            try (PreparedStatement ps = con.prepareStatement(SQL_LISTAR);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error listando prácticas: " + e.getMessage(), e);
        } finally {
            ConexionDB.liberar(con);
        }
        return lista;
    }

    /** Lista prácticas activas de un programa, ordenadas por número. */
    public List<Practica> listarPorPrograma(int idPrograma) {
        List<Practica> lista = new ArrayList<>();
        Connection con = null;
        try {
            con = ConexionDB.getConexion();
            try (PreparedStatement ps = con.prepareStatement(SQL_LISTAR_POR_PROGRAMA)) {
                ps.setInt(1, idPrograma);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error listando por programa: " + e.getMessage(), e);
        } finally {
            ConexionDB.liberar(con);
        }
        return lista;
    }

    /** Cuenta prácticas activas del programa (máximo permitido: 8). */
    public int contarPorPrograma(int idPrograma) {
        Connection con = null;
        try {
            con = ConexionDB.getConexion();
            try (PreparedStatement ps = con.prepareStatement(SQL_CONTAR_POR_PROGRAMA)) {
                ps.setInt(1, idPrograma);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error contando prácticas: " + e.getMessage(), e);
        } finally {
            ConexionDB.liberar(con);
        }
        return 0;
    }

    /** Cambia el estado de una práctica (Planificada→Activa→Finalizada). */
    public boolean cambiarEstado(int id, String nuevoEstado) {
        Connection con = null;
        try {
            con = ConexionDB.getConexion();
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(SQL_CAMBIAR_ESTADO)) {
                ps.setString(1, nuevoEstado);
                ps.setInt(2, id);
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

    // ── Mapeo ─────────────────────────────────────────────────────────────────

    private Practica mapear(ResultSet rs) throws SQLException {
        Practica pr = new Practica();
        pr.setId(rs.getInt("id"));
        pr.setIdPrograma(rs.getInt("id_programa"));
        pr.setNumero(rs.getInt("numero"));
        pr.setNombre(rs.getString("nombre"));
        pr.setTipo(rs.getString("tipo"));
        pr.setObjetivos(rs.getString("objetivos"));
        pr.setHorasMinimas(rs.getInt("horas_minimas"));
        pr.setSemestre(rs.getString("semestre"));

        // Oracle DATE → java.sql.Date → LocalDate
        Date di = rs.getDate("fecha_inicio");
        if (di != null) pr.setFechaInicio(di.toLocalDate());
        Date df = rs.getDate("fecha_fin");
        if (df != null) pr.setFechaFin(df.toLocalDate());

        pr.setEstado(rs.getString("estado"));

        Timestamp fc = rs.getTimestamp("fecha_creacion");
        if (fc != null) pr.setFechaCreacion(fc.toLocalDateTime());

        pr.setActivo(rs.getInt("activo") == 1);

        try { pr.setNombrePrograma(rs.getString("nombre_programa")); }
        catch (SQLException ignored) {}

        return pr;
    }

    private void rollback(Connection c) {
        if (c != null) try { c.rollback(); } catch (SQLException e) {}
    }
    private void resetAC(Connection c) {
        if (c != null) try { c.setAutoCommit(true); } catch (SQLException e) {}
    }
}
