package practicaspedagogicas.dao;

import practicaspedagogicas.modelo.InstitucionReceptora;
import practicaspedagogicas.util.ConexionDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InstitucionReceptoraDAO implements DAO<InstitucionReceptora, Integer> {

    private static final Logger LOG = Logger.getLogger(InstitucionReceptoraDAO.class.getName());

    private static final String SQL_INSERTAR =
        "INSERT INTO institucion_receptora (id, nombre, nit, dane, direccion, municipio, " +
        "departamento, zona, nivel_educativo, nombre_rector, correo_contacto, telefono) " +
        "VALUES (SEQ_INSTITUCION_RECEPTORA.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_ULTIMO_ID =
        "SELECT SEQ_INSTITUCION_RECEPTORA.CURRVAL FROM DUAL";

    private static final String SQL_ACTUALIZAR =
        "UPDATE institucion_receptora SET nombre=?, nit=?, dane=?, direccion=?, municipio=?, " +
        "departamento=?, zona=?, nivel_educativo=?, nombre_rector=?, correo_contacto=?, " +
        "telefono=? WHERE id=?";

    private static final String SQL_ELIMINAR =
        "UPDATE institucion_receptora SET activo=0 WHERE id=?";

    private static final String SQL_BUSCAR_POR_ID =
        "SELECT * FROM institucion_receptora WHERE id=? AND activo=1";

    private static final String SQL_LISTAR =
        "SELECT * FROM institucion_receptora WHERE activo=1 ORDER BY nombre";

    private static final String SQL_BUSCAR_POR_MUNICIPIO =
        "SELECT * FROM institucion_receptora WHERE municipio LIKE ? AND activo=1 ORDER BY nombre";

    @Override
    public boolean insertar(InstitucionReceptora inst) {
        Connection con = null;
        try {
            con = ConexionDB.getConexion(); con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(SQL_INSERTAR)) {
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
                    try (Statement st = con.createStatement();
                         ResultSet rs = st.executeQuery(SQL_ULTIMO_ID)) {
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
