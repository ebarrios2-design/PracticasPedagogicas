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
class GrupoDAO implements DAO<Grupo, Integer> {

    private static final Logger LOG = Logger.getLogger(GrupoDAO.class.getName());

    private static final String SQL_INSERTAR =
        "INSERT INTO grupo (id_practica, id_institucion, nombre, cupo_maximo, jornada, observaciones) " +
        "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SQL_ACTUALIZAR =
        "UPDATE grupo SET id_practica=?, id_institucion=?, nombre=?, cupo_maximo=?, " +
        "jornada=?, observaciones=? WHERE id=?";

    private static final String SQL_ELIMINAR =
        "UPDATE grupo SET activo=FALSE WHERE id=?";

    private static final String SQL_BUSCAR_POR_ID =
        "SELECT g.*, pr.nombre AS nombre_practica, ins.nombre AS nombre_institucion, " +
        "(SELECT COUNT(*) FROM grupo_estudiante ge WHERE ge.id_grupo=g.id AND ge.estado='Activo') AS inscritos " +
        "FROM grupo g " +
        "JOIN practica pr ON g.id_practica = pr.id " +
        "JOIN institucion_receptora ins ON g.id_institucion = ins.id " +
        "WHERE g.id=? AND g.activo=TRUE";

    private static final String SQL_LISTAR =
        "SELECT g.*, pr.nombre AS nombre_practica, ins.nombre AS nombre_institucion, " +
        "(SELECT COUNT(*) FROM grupo_estudiante ge WHERE ge.id_grupo=g.id AND ge.estado='Activo') AS inscritos " +
        "FROM grupo g " +
        "JOIN practica pr ON g.id_practica = pr.id " +
        "JOIN institucion_receptora ins ON g.id_institucion = ins.id " +
        "WHERE g.activo=TRUE ORDER BY pr.numero, g.nombre";

    private static final String SQL_LISTAR_POR_PRACTICA =
        "SELECT g.*, pr.nombre AS nombre_practica, ins.nombre AS nombre_institucion, " +
        "(SELECT COUNT(*) FROM grupo_estudiante ge WHERE ge.id_grupo=g.id AND ge.estado='Activo') AS inscritos " +
        "FROM grupo g " +
        "JOIN practica pr ON g.id_practica = pr.id " +
        "JOIN institucion_receptora ins ON g.id_institucion = ins.id " +
        "WHERE g.id_practica=? AND g.activo=TRUE ORDER BY g.nombre";

    @Override
    public boolean insertar(Grupo g) {
        Connection con = null;
        try {
            con = ConexionDB.getConexion();
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(
                    SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, g.getIdPractica());
                ps.setInt(2, g.getIdInstitucion());
                ps.setString(3, g.getNombre());
                ps.setInt(4, g.getCupoMaximo());
                ps.setString(5, g.getJornada());
                ps.setString(6, g.getObservaciones());
                int filas = ps.executeUpdate();
                if (filas > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
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
class InstitucionReceptoraDAO implements DAO<InstitucionReceptora, Integer> {

    private static final Logger LOG = Logger.getLogger(InstitucionReceptoraDAO.class.getName());

    private static final String SQL_INSERTAR =
        "INSERT INTO institucion_receptora (nombre, nit, dane, direccion, municipio, " +
        "departamento, zona, nivel_educativo, nombre_rector, correo_contacto, telefono) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_ACTUALIZAR =
        "UPDATE institucion_receptora SET nombre=?, nit=?, dane=?, direccion=?, municipio=?, " +
        "departamento=?, zona=?, nivel_educativo=?, nombre_rector=?, correo_contacto=?, " +
        "telefono=? WHERE id=?";

    private static final String SQL_ELIMINAR =
        "UPDATE institucion_receptora SET activo=FALSE WHERE id=?";

    private static final String SQL_BUSCAR_POR_ID =
        "SELECT * FROM institucion_receptora WHERE id=? AND activo=TRUE";

    private static final String SQL_LISTAR =
        "SELECT * FROM institucion_receptora WHERE activo=TRUE ORDER BY nombre";

    private static final String SQL_BUSCAR_POR_MUNICIPIO =
        "SELECT * FROM institucion_receptora WHERE municipio LIKE ? AND activo=TRUE ORDER BY nombre";

    @Override
    public boolean insertar(InstitucionReceptora inst) {
        Connection con = null;
        try {
            con = ConexionDB.getConexion(); con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(
                    SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, inst.getNombre());
                ps.setString(2, inst.getNit());
                ps.setString(3, inst.getDane());
                ps.setString(4, inst.getDireccion());
                ps.setString(5, inst.getMunicipio());
                ps.setString(6, inst.getDepartamento());
                ps.setString(7, inst.getZona());
                ps.setString(8, inst.getNivelEducativo());
                ps.setString(9, inst.getNombreRector());
                ps.setString(10, inst.getCorreoContacto());
                ps.setString(11, inst.getTelefono());
                int f = ps.executeUpdate();
                if (f > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) inst.setId(rs.getInt(1));
                    }
                    con.commit(); return true;
                }
                con.rollback(); return false;
            }
        } catch (SQLException e) {
            rollback(con);
            LOG.log(Level.SEVERE, "Error insertando institución: " + e.getMessage(), e);
            return false;
        } finally { resetAC(con); ConexionDB.liberar(con); }
    }

    @Override
    public boolean actualizar(InstitucionReceptora inst) {
        Connection con = null;
        try {
            con = ConexionDB.getConexion(); con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(SQL_ACTUALIZAR)) {
                ps.setString(1,  inst.getNombre());
                ps.setString(2,  inst.getNit());
                ps.setString(3,  inst.getDane());
                ps.setString(4,  inst.getDireccion());
                ps.setString(5,  inst.getMunicipio());
                ps.setString(6,  inst.getDepartamento());
                ps.setString(7,  inst.getZona());
                ps.setString(8,  inst.getNivelEducativo());
                ps.setString(9,  inst.getNombreRector());
                ps.setString(10, inst.getCorreoContacto());
                ps.setString(11, inst.getTelefono());
                ps.setInt(12, inst.getId());
                int f = ps.executeUpdate(); con.commit(); return f > 0;
            }
        } catch (SQLException e) {
            rollback(con); return false;
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
    public InstitucionReceptora buscarPorId(Integer id) {
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
            LOG.log(Level.SEVERE, "Error buscando institución: " + e.getMessage(), e);
        } finally { ConexionDB.liberar(con); }
        return null;
    }

    @Override
    public List<InstitucionReceptora> listarTodos() {
        List<InstitucionReceptora> lista = new ArrayList<>();
        Connection con = null;
        try {
            con = ConexionDB.getConexion();
            try (PreparedStatement ps = con.prepareStatement(SQL_LISTAR);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error listando instituciones: " + e.getMessage(), e);
        } finally { ConexionDB.liberar(con); }
        return lista;
    }

    /** Busca instituciones por municipio (parcial). */
    public List<InstitucionReceptora> buscarPorMunicipio(String municipio) {
        List<InstitucionReceptora> lista = new ArrayList<>();
        Connection con = null;
        try {
            con = ConexionDB.getConexion();
            try (PreparedStatement ps = con.prepareStatement(SQL_BUSCAR_POR_MUNICIPIO)) {
                ps.setString(1, "%" + municipio + "%");
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error buscando por municipio: " + e.getMessage(), e);
        } finally { ConexionDB.liberar(con); }
        return lista;
    }

    private InstitucionReceptora mapear(ResultSet rs) throws SQLException {
        InstitucionReceptora i = new InstitucionReceptora();
        i.setId(rs.getInt("id"));
        i.setNombre(rs.getString("nombre"));
        i.setNit(rs.getString("nit"));
        i.setDane(rs.getString("dane"));
        i.setDireccion(rs.getString("direccion"));
        i.setMunicipio(rs.getString("municipio"));
        i.setDepartamento(rs.getString("departamento"));
        i.setZona(rs.getString("zona"));
        i.setNivelEducativo(rs.getString("nivel_educativo"));
        i.setNombreRector(rs.getString("nombre_rector"));
        i.setCorreoContacto(rs.getString("correo_contacto"));
        i.setTelefono(rs.getString("telefono"));
        i.setActivo(rs.getBoolean("activo"));
        return i;
    }

    private void rollback(Connection c) { if(c!=null) try{c.rollback();}catch(SQLException e){} }
    private void resetAC(Connection c)  { if(c!=null) try{c.setAutoCommit(true);}catch(SQLException e){} }
}
