package practicaspedagogicas.dao;

import practicaspedagogicas.modelo.Grupo;
import practicaspedagogicas.modelo.InstitucionReceptora;
import practicaspedagogicas.util.ConexionDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

// ══════════════════════════════════════════════════════════════════════════════
//  GrupoDAO
// ══════════════════════════════════════════════════════════════════════════════

/**
 * GrupoDAO - Acceso a datos para la entidad Grupo.
 * Gestiona grupos de práctica con control de cupo.
 *
 * @version 1.0
 */
public class GrupoDAO implements DAO<Grupo, Integer> {

    private static final Logger LOG = Logger.getLogger(GrupoDAO.class.getName());

    private static final String SQL_INSERTAR =
        "INSERT INTO grupo (id, id_practica, id_institucion, nombre, cupo_maximo, jornada, observaciones) " +
        "VALUES (SEQ_GRUPO.NEXTVAL, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_ULTIMO_ID =
        "SELECT SEQ_GRUPO.CURRVAL FROM DUAL";

    private static final String SQL_ACTUALIZAR =
        "UPDATE grupo SET id_practica=?, id_institucion=?, nombre=?, cupo_maximo=?, " +
        "jornada=?, observaciones=? WHERE id=?";

    private static final String SQL_ELIMINAR =
        "UPDATE grupo SET activo=0 WHERE id=?";

    private static final String SQL_BUSCAR_POR_ID =
        "SELECT g.*, pr.nombre AS nombre_practica, ins.nombre AS nombre_institucion, " +
        "(SELECT COUNT(*) FROM grupo_estudiante ge WHERE ge.id_grupo=g.id AND ge.estado='Activo') AS inscritos " +
        "FROM grupo g " +
        "JOIN practica pr ON g.id_practica = pr.id " +
        "JOIN institucion_receptora ins ON g.id_institucion = ins.id " +
        "WHERE g.id=? AND g.activo=1";

    private static final String SQL_LISTAR =
        "SELECT g.*, pr.nombre AS nombre_practica, ins.nombre AS nombre_institucion, " +
        "(SELECT COUNT(*) FROM grupo_estudiante ge WHERE ge.id_grupo=g.id AND ge.estado='Activo') AS inscritos " +
        "FROM grupo g " +
        "JOIN practica pr ON g.id_practica = pr.id " +
        "JOIN institucion_receptora ins ON g.id_institucion = ins.id " +
        "WHERE g.activo=1 ORDER BY pr.numero, g.nombre";

    private static final String SQL_LISTAR_POR_PRACTICA =
        "SELECT g.*, pr.nombre AS nombre_practica, ins.nombre AS nombre_institucion, " +
        "(SELECT COUNT(*) FROM grupo_estudiante ge WHERE ge.id_grupo=g.id AND ge.estado='Activo') AS inscritos " +
        "FROM grupo g " +
        "JOIN practica pr ON g.id_practica = pr.id " +
        "JOIN institucion_receptora ins ON g.id_institucion = ins.id " +
        "WHERE g.id_practica=? AND g.activo=1 ORDER BY g.nombre";

    @Override
    public boolean insertar(Grupo g) {
        Connection con = null;
        try {
            con = ConexionDB.getConexion();
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(SQL_INSERTAR)) {
                ps.setInt(1, g.getIdPractica());
                ps.setInt(2, g.getIdInstitucion());
                ps.setString(3, g.getNombre());
                ps.setInt(4, g.getCupoMaximo());
                ps.setString(5, g.getJornada());
                ps.setString(6, g.getObservaciones());
                int filas = ps.executeUpdate();
                if (filas > 0) {
                    try (Statement st = con.createStatement();
                         ResultSet rs = st.executeQuery(SQL_ULTIMO_ID)) {
                        if (rs.next()) g.setId(rs.getInt(1));
                    }
                    con.commit();
                    return true;
                }
                con.rollback(); return false;
            }
        } catch (SQLException e) {
            rollback(con);
            LOG.log(Level.SEVERE, "Error insertando grupo: " + e.getMessage(), e);
            return false;
        } finally { resetAC(con); ConexionDB.liberar(con); }
    }

    @Override
    public boolean actualizar(Grupo g) {
        Connection con = null;
        try {
            con = ConexionDB.getConexion(); con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(SQL_ACTUALIZAR)) {
                ps.setInt(1, g.getIdPractica());
                ps.setInt(2, g.getIdInstitucion());
                ps.setString(3, g.getNombre());
                ps.setInt(4, g.getCupoMaximo());
                ps.setString(5, g.getJornada());
                ps.setString(6, g.getObservaciones());
                ps.setInt(7, g.getId());
                int f = ps.executeUpdate(); con.commit(); return f > 0;
            }
        } catch (SQLException e) {
            rollback(con);
            LOG.log(Level.SEVERE, "Error actualizando grupo: " + e.getMessage(), e);
            return false;
        } finally { resetAC(con); ConexionDB.liberar(con); }
    }

    @Override
    public boolean eliminar(Integer id) {
        Connection con = null;
        try {
            con = ConexionDB.getConexion(); con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(SQL_ELIMINAR)) {
                ps.setInt(1, id);
                int f = ps.executeUpdate(); con.commit(); return f > 0;
            }
        } catch (SQLException e) {
            rollback(con); return false;
        } finally { resetAC(con); ConexionDB.liberar(con); }
    }

    @Override
    public Grupo buscarPorId(Integer id) {
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
            LOG.log(Level.SEVERE, "Error buscando grupo: " + e.getMessage(), e);
        } finally { ConexionDB.liberar(con); }
        return null;
    }

    @Override
    public List<Grupo> listarTodos() {
        List<Grupo> lista = new ArrayList<>();
        Connection con = null;
        try {
            con = ConexionDB.getConexion();
            try (PreparedStatement ps = con.prepareStatement(SQL_LISTAR);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error listando grupos: " + e.getMessage(), e);
        } finally { ConexionDB.liberar(con); }
        return lista;
    }

    /** Lista grupos de una práctica específica. */
    public List<Grupo> listarPorPractica(int idPractica) {
        List<Grupo> lista = new ArrayList<>();
        Connection con = null;
        try {
            con = ConexionDB.getConexion();
            try (PreparedStatement ps = con.prepareStatement(SQL_LISTAR_POR_PRACTICA)) {
                ps.setInt(1, idPractica);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error listando grupos por práctica: " + e.getMessage(), e);
        } finally { ConexionDB.liberar(con); }
        return lista;
    }

    private Grupo mapear(ResultSet rs) throws SQLException {
        Grupo g = new Grupo();
        g.setId(rs.getInt("id"));
        g.setIdPractica(rs.getInt("id_practica"));
        g.setIdInstitucion(rs.getInt("id_institucion"));
        g.setNombre(rs.getString("nombre"));
        g.setCupoMaximo(rs.getInt("cupo_maximo"));
        g.setJornada(rs.getString("jornada"));
        g.setObservaciones(rs.getString("observaciones"));
        Timestamp fc = rs.getTimestamp("fecha_creacion");
        if (fc != null) g.setFechaCreacion(fc.toLocalDateTime());
        g.setActivo(rs.getBoolean("activo"));
        try { g.setNombrePractica(rs.getString("nombre_practica")); } catch (SQLException ignored) {}
        try { g.setNombreInstitucion(rs.getString("nombre_institucion")); } catch (SQLException ignored) {}
        try { g.setEstudiantesInscritos(rs.getInt("inscritos")); } catch (SQLException ignored) {}
        return g;
    }

    private void rollback(Connection c) { if(c!=null) try{c.rollback();}catch(SQLException e){} }
    private void resetAC(Connection c)  { if(c!=null) try{c.setAutoCommit(true);}catch(SQLException e){} }
}


// ══════════════════════════════════════════════════════════════════════════════
//  InstitucionReceptoraDAO
// ══════════════════════════════════════════════════════════════════════════════

/**
 * InstitucionReceptoraDAO - CRUD para instituciones educativas receptoras.
 *
 * @version 1.0
 */
