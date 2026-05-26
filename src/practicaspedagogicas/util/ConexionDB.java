package practicaspedagogicas.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ConexionDB - Gestiona la conexión a Oracle 10g XE mediante JDBC.
 * Implementa un pool simple de conexiones reutilizables.
 *
 * Driver requerido: ojdbc14.jar  (viene con Oracle 10g XE)
 * Ruta típica: C:\oraclexe\app\oracle\product\10.2.0\server\jdbc\lib\ojdbc14.jar
 *
 * Uso:
 *   Connection con = ConexionDB.getConexion();
 *   // ... operaciones ...
 *   ConexionDB.liberar(con);
 *
 * @version 1.0 - Oracle 10g XE
 */
public class ConexionDB {

    private static final Logger LOG = Logger.getLogger(ConexionDB.class.getName());

    private static String URL;
    private static String USER;
    private static String PASSWORD;
    private static int    POOL_SIZE;

    private static final Queue<Connection> POOL = new LinkedList<>();

    static {
        cargarPropiedades();
        inicializarPool();
    }

    private ConexionDB() {}

    // ── Carga propiedades desde db.properties ─────────────────────────────────
    private static void cargarPropiedades() {
        try (InputStream is = ConexionDB.class
                .getResourceAsStream("db.properties")) {

            Properties props = new Properties();
            props.load(is);

            String driver = props.getProperty("db.driver");
            URL       = props.getProperty("db.url");
            USER      = props.getProperty("db.user");
            PASSWORD  = props.getProperty("db.password");
            POOL_SIZE = Integer.parseInt(props.getProperty("db.pool.size", "5"));

            // Cargar driver Oracle
            Class.forName(driver);
            LOG.info("Driver Oracle cargado: " + driver);

        } catch (ClassNotFoundException e) {
            LOG.severe("Driver Oracle no encontrado. Agregue ojdbc14.jar a las librerías del proyecto.");
            throw new RuntimeException("Driver Oracle no encontrado: " + e.getMessage(), e);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error cargando db.properties: " + e.getMessage(), e);
            throw new RuntimeException("No se pudo cargar la configuración.", e);
        }
    }

    // ── Inicializa el pool ────────────────────────────────────────────────────
    private static void inicializarPool() {
        for (int i = 0; i < POOL_SIZE; i++) {
            try {
                Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
                // Oracle 10g: deshabilitar autocommit por defecto
                con.setAutoCommit(true);
                POOL.offer(con);
            } catch (SQLException e) {
                LOG.log(Level.WARNING,
                    "No se pudo crear conexión " + (i + 1) + " del pool: " + e.getMessage(), e);
            }
        }
        LOG.info("Pool Oracle inicializado con " + POOL.size() + " conexiones.");
    }

    /**
     * Obtiene una conexión activa del pool.
     * Si el pool está vacío o la conexión expiró, crea una nueva.
     *
     * @return Connection hacia Oracle XE.
     */
    public static synchronized Connection getConexion() {
        Connection con = POOL.poll();
        try {
            if (con == null || con.isClosed()) {
                LOG.info("Creando nueva conexión Oracle (pool agotado).");
                con = DriverManager.getConnection(URL, USER, PASSWORD);
            }
            // Oracle 10g no tiene isValid(timeout) en ojdbc14 → usamos isClosed()
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error obteniendo conexión: " + e.getMessage(), e);
            throw new RuntimeException("No se pudo obtener conexión a Oracle.", e);
        }
        return con;
    }

    /**
     * Devuelve una conexión al pool para reutilizarla.
     *
     * @param con Conexión a liberar.
     */
    public static synchronized void liberar(Connection con) {
        if (con != null) {
            try {
                // Asegurar estado limpio antes de reusar
                if (!con.isClosed()) {
                    con.setAutoCommit(true);
                    POOL.offer(con);
                }
            } catch (SQLException e) {
                LOG.log(Level.WARNING, "Error liberando conexión.", e);
            }
        }
    }

    /** Cierra todas las conexiones del pool al cerrar la aplicación. */
    public static synchronized void cerrarPool() {
        for (Connection con : POOL) {
            try {
                if (con != null && !con.isClosed()) con.close();
            } catch (SQLException e) {
                LOG.log(Level.WARNING, "Error cerrando conexión del pool.", e);
            }
        }
        POOL.clear();
        LOG.info("Pool Oracle cerrado.");
    }

    /**
     * Prueba la conexión a Oracle XE.
     *
     * @return true si la conexión es exitosa.
     */
    public static boolean probarConexion() {
        try {
            Connection con = getConexion();
            boolean ok = con != null && !con.isClosed();
            liberar(con);
            return ok;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Prueba de conexión fallida: " + e.getMessage(), e);
            return false;
        }
    }
}
