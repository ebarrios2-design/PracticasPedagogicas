package practicaspedagogicas.vista.panels;

import practicaspedagogicas.util.ConexionDB;
import practicaspedagogicas.util.SesionUsuario;
import practicaspedagogicas.modelo.Usuario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * MiEvaluacionPanel - Panel del Estudiante para consultar su evaluación
 * final, retroalimentaciones de cuestionarios y resumen de su práctica.
 *
 * @version 1.0 - Oracle 10g XE
 */
public class MiEvaluacionPanel extends JPanel {

    private static final Logger LOG = Logger.getLogger(MiEvaluacionPanel.class.getName());
    private final Usuario estudiante = SesionUsuario.getInstance().getUsuario();

    // ── Resumen práctica ───────────────────────────────────────────────────────
    private JLabel lblPractica, lblGrupo, lblInstitucion, lblDocente;
    private JLabel lblHorasCum, lblHorasReq, lblEstadoHoras;
    private JProgressBar progHoras;

    // ── Evaluación final ───────────────────────────────────────────────────────
    private JLabel   lblNota, lblAprobado, lblFechaEval;
    private JTextArea txtObsDocente;

    // ── Tabla retroalimentaciones ──────────────────────────────────────────────
    private JTable            tablaRetro;
    private DefaultTableModel modeloRetro;

    private static final Color AZUL   = new Color(0x1F, 0x49, 0x7D);
    private static final Color AZUL_M = new Color(0x2E, 0x75, 0xB6);
    private static final Color VERDE  = new Color(0x27, 0xAE, 0x60);
    private static final Color ROJO   = new Color(0xC0, 0x39, 0x2B);
    private static final Color BG     = new Color(0xF4, 0xF7, 0xFB);

    public MiEvaluacionPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BG);
        setBorder(new EmptyBorder(14, 14, 14, 14));
        initUI();
        cargarDatos();
    }

    private void initUI() {
        // ── Título ─────────────────────────────────────────────────────────────
        JPanel norte = new JPanel(new BorderLayout());
        norte.setBackground(BG);
        JLabel lbl = new JLabel("Mi Evaluación de Práctica");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lbl.setForeground(AZUL);
        JButton btnRefresh = new JButton("↺ Actualizar");
        btnRefresh.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnRefresh.setBackground(AZUL_M); btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setBorderPainted(false); btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> cargarDatos());
        norte.add(lbl, BorderLayout.WEST);
        norte.add(btnRefresh, BorderLayout.EAST);
        norte.setBorder(new EmptyBorder(0,0,10,0));
        add(norte, BorderLayout.NORTH);

        // ── Layout principal: 3 columnas ───────────────────────────────────────
        JPanel contenido = new JPanel(new GridLayout(1, 3, 10, 0));
        contenido.setBackground(BG);

        // ── Card 1: Datos de la práctica ──────────────────────────────────────
        JPanel cardPrac = card("📋 Mi Práctica Asignada");
        lblPractica    = linea(cardPrac, "Práctica:",    "--");
        lblGrupo       = linea(cardPrac, "Grupo:",       "--");
        lblInstitucion = linea(cardPrac, "Institución:", "--");
        lblDocente     = linea(cardPrac, "Docente:",     "--");
        cardPrac.add(Box.createVerticalStrut(14));

        JLabel lHoras = new JLabel("Cumplimiento de horas:");
        lHoras.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lHoras.setForeground(AZUL); lHoras.setAlignmentX(LEFT_ALIGNMENT);
        cardPrac.add(lHoras);
        cardPrac.add(Box.createVerticalStrut(6));

        lblHorasCum   = info("Aprobadas: 0 h");
        lblHorasReq   = info("Requeridas: -- h");
        lblEstadoHoras= info("Estado: Pendiente");
        cardPrac.add(lblHorasCum);
        cardPrac.add(Box.createVerticalStrut(4));
        cardPrac.add(lblHorasReq);
        cardPrac.add(Box.createVerticalStrut(4));
        cardPrac.add(lblEstadoHoras);
        cardPrac.add(Box.createVerticalStrut(8));

        progHoras = new JProgressBar(0, 100);
        progHoras.setStringPainted(true); progHoras.setString("0%");
        progHoras.setForeground(AZUL_M);
        progHoras.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        progHoras.setAlignmentX(LEFT_ALIGNMENT);
        cardPrac.add(progHoras);
        contenido.add(cardPrac);

        // ── Card 2: Evaluación final ───────────────────────────────────────────
        JPanel cardEval = card("📊 Evaluación Final");
        cardEval.add(Box.createVerticalStrut(8));

        JPanel panNota = new JPanel(new GridBagLayout());
        panNota.setBackground(Color.WHITE);
        panNota.setAlignmentX(LEFT_ALIGNMENT);
        panNota.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        lblNota = new JLabel("--");
        lblNota.setFont(new Font("Segoe UI", Font.BOLD, 48));
        lblNota.setForeground(AZUL_M);
        panNota.add(lblNota);
        cardEval.add(panNota);
        cardEval.add(Box.createVerticalStrut(8));

        lblAprobado  = info("Estado: Sin evaluar");
        lblFechaEval = info("Fecha: --");
        cardEval.add(lblAprobado);
        cardEval.add(Box.createVerticalStrut(4));
        cardEval.add(lblFechaEval);
        cardEval.add(Box.createVerticalStrut(12));

        JLabel lObs = new JLabel("Observación del docente:");
        lObs.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lObs.setForeground(AZUL); lObs.setAlignmentX(LEFT_ALIGNMENT);
        cardEval.add(lObs);
        cardEval.add(Box.createVerticalStrut(4));

        txtObsDocente = new JTextArea(5, 16);
        txtObsDocente.setEditable(false);
        txtObsDocente.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        txtObsDocente.setLineWrap(true); txtObsDocente.setWrapStyleWord(true);
        txtObsDocente.setBackground(new Color(0xF9, 0xF9, 0xF9));
        txtObsDocente.setText("Sin evaluación registrada aún.");
        JScrollPane scrObs = new JScrollPane(txtObsDocente);
        scrObs.setAlignmentX(LEFT_ALIGNMENT);
        scrObs.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        scrObs.setBorder(BorderFactory.createLineBorder(new Color(0xE0,0xE0,0xE0)));
        cardEval.add(scrObs);
        contenido.add(cardEval);

        // ── Card 3: Retroalimentaciones ────────────────────────────────────────
        JPanel cardRetro = card("💬 Retroalimentaciones del Docente");
        String[] colsR = {"Pregunta","Mi respuesta","Retroalimentación"};
        modeloRetro = new DefaultTableModel(colsR, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaRetro = new JTable(modeloRetro);
        tablaRetro.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tablaRetro.setRowHeight(50);
        tablaRetro.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        tablaRetro.getTableHeader().setBackground(AZUL);
        tablaRetro.getTableHeader().setForeground(Color.WHITE);
        tablaRetro.setGridColor(new Color(0xE0,0xE0,0xE0));
        tablaRetro.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        // Wrap en celdas
        tablaRetro.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                JTextArea ta = new JTextArea(v != null ? v.toString() : "");
                ta.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                ta.setLineWrap(true); ta.setWrapStyleWord(true);
                ta.setBackground(sel ? new Color(0xD6,0xE4,0xF0) : (row%2==0 ? Color.WHITE : new Color(0xF5,0xF5,0xF5)));
                ta.setBorder(new EmptyBorder(4,6,4,6));
                return ta;
            }
        });

        JScrollPane scrRetro = new JScrollPane(tablaRetro);
        scrRetro.setAlignmentX(LEFT_ALIGNMENT);
        scrRetro.setBorder(BorderFactory.createLineBorder(new Color(0xC0,0xC0,0xC0)));
        cardRetro.add(scrRetro);
        contenido.add(cardRetro);

        add(contenido, BorderLayout.CENTER);
    }

    // ── Carga de datos desde Oracle ───────────────────────────────────────────

    private void cargarDatos() {
        if (estudiante == null) return;
        cargarDatosPractica();
        cargarEvaluacionFinal();
        cargarRetroalimentaciones();
    }

    private void cargarDatosPractica() {
        String sql =
            "SELECT pr.nombre AS practica, g.nombre AS grupo, " +
            "inst.nombre AS institucion, " +
            "u.nombres || ' ' || u.apellidos AS docente, " +
            "pr.horas_minimas, FN_HORAS_CUMPLIDAS(ge.id) AS horas_ok, " +
            "FN_PORCENTAJE_HORAS(ge.id) AS pct, ge.id AS id_ge " +
            "FROM grupo_estudiante ge " +
            "JOIN grupo g    ON ge.id_grupo    = g.id " +
            "JOIN practica pr ON g.id_practica = pr.id " +
            "JOIN institucion_receptora inst ON g.id_institucion = inst.id " +
            "LEFT JOIN grupo_docente gd ON gd.id_grupo = g.id AND gd.es_principal = 1 " +
            "LEFT JOIN usuario u ON gd.id_docente = u.id " +
            "WHERE ge.id_estudiante = ? AND ge.estado = 'Activo' AND ROWNUM = 1";

        Connection con = null;
        try {
            con = ConexionDB.getConexion();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, estudiante.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        lblPractica.setText("Práctica: " + rs.getString("practica"));
                        lblGrupo.setText("Grupo: " + rs.getString("grupo"));
                        lblInstitucion.setText("Institución: " + acortar(rs.getString("institucion"), 30));
                        String doc = rs.getString("docente");
                        lblDocente.setText("Docente: " + (doc != null ? doc : "Sin asignar"));

                        double cumpl = rs.getDouble("horas_ok");
                        int    req   = rs.getInt("horas_minimas");
                        int    pct   = (int) Math.min(100, rs.getDouble("pct"));

                        lblHorasCum.setText(String.format("Aprobadas: %.1f h", cumpl));
                        lblHorasReq.setText("Requeridas: " + req + " h");
                        progHoras.setValue(pct);
                        progHoras.setString(pct + "%");

                        if (pct >= 100) {
                            lblEstadoHoras.setText("Estado: ✅ Cumplidas");
                            lblEstadoHoras.setForeground(VERDE);
                            progHoras.setForeground(VERDE);
                        } else if (pct >= 50) {
                            lblEstadoHoras.setText("Estado: ⚠ En progreso");
                            lblEstadoHoras.setForeground(new Color(0xF3,0x9C,0x12));
                            progHoras.setForeground(new Color(0xF3,0x9C,0x12));
                        } else {
                            lblEstadoHoras.setText("Estado: ❌ Insuficiente");
                            lblEstadoHoras.setForeground(ROJO);
                            progHoras.setForeground(ROJO);
                        }
                    } else {
                        lblPractica.setText("Sin práctica activa asignada.");
                    }
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error cargando práctica del estudiante.", e);
        } finally { ConexionDB.liberar(con); }
    }

    private void cargarEvaluacionFinal() {
        String sql =
            "SELECT ef.nota_definitiva, ef.aprobado, ef.observacion_general, " +
            "ef.fecha_evaluacion " +
            "FROM evaluacion_final ef " +
            "JOIN grupo_estudiante ge ON ef.id_grupo_est = ge.id " +
            "WHERE ge.id_estudiante = ? AND ROWNUM = 1 " +
            "ORDER BY ef.fecha_evaluacion DESC";

        Connection con = null;
        try {
            con = ConexionDB.getConexion();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, estudiante.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        double nota  = rs.getDouble("nota_definitiva");
                        int    apro  = rs.getInt("aprobado");
                        String obs   = rs.getString("observacion_general");
                        Timestamp fe = rs.getTimestamp("fecha_evaluacion");

                        lblNota.setText(String.format("%.2f", nota));
                        lblNota.setForeground(apro == 1 ? VERDE : ROJO);

                        if (apro == 1) {
                            lblAprobado.setText("Estado: ✅ APROBADO");
                            lblAprobado.setForeground(VERDE);
                        } else {
                            lblAprobado.setText("Estado: ❌ REPROBADO");
                            lblAprobado.setForeground(ROJO);
                        }
                        if (fe != null)
                            lblFechaEval.setText("Fecha: " + fe.toLocalDateTime().toLocalDate());

                        txtObsDocente.setText(obs != null ? obs : "Sin observación registrada.");
                    } else {
                        lblNota.setText("--");
                        lblAprobado.setText("Sin evaluación aún.");
                        lblAprobado.setForeground(new Color(0x80,0x80,0x80));
                    }
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error cargando evaluación.", e);
        } finally { ConexionDB.liberar(con); }
    }

    private void cargarRetroalimentaciones() {
        modeloRetro.setRowCount(0);
        String sql =
            "SELECT bp.enunciado, re.respuesta_texto, re.respuesta_opcion, re.retroalimentacion " +
            "FROM respuesta_estudiante re " +
            "JOIN banco_preguntas bp ON re.id_pregunta = re.id_pregunta " +
            "WHERE re.id_estudiante = ? AND re.retroalimentacion IS NOT NULL " +
            "ORDER BY re.fecha_respuesta DESC";

        Connection con = null;
        try {
            con = ConexionDB.getConexion();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, estudiante.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String pregunta = acortar(rs.getString("enunciado"), 60);
                        String respuesta = rs.getString("respuesta_texto");
                        if (respuesta == null) respuesta = rs.getString("respuesta_opcion");
                        String retro = rs.getString("retroalimentacion");
                        modeloRetro.addRow(new Object[]{
                            pregunta,
                            respuesta != null ? respuesta : "",
                            retro != null ? retro : ""
                        });
                    }
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error cargando retroalimentaciones.", e);
        } finally { ConexionDB.liberar(con); }
    }

    // ── Helpers UI ────────────────────────────────────────────────────────────

    private JPanel card(String titulo) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xCC,0xCC,0xCC)),
            new EmptyBorder(14, 14, 14, 14)));
        JLabel t = new JLabel(titulo);
        t.setFont(new Font("Segoe UI", Font.BOLD, 13));
        t.setForeground(AZUL); t.setAlignmentX(LEFT_ALIGNMENT);
        p.add(t);
        p.add(Box.createVerticalStrut(10));
        return p;
    }

    private JLabel linea(JPanel p, String etq, String val) {
        JLabel l = new JLabel(etq + " " + val);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setAlignmentX(LEFT_ALIGNMENT);
        p.add(l); p.add(Box.createVerticalStrut(4));
        return l;
    }

    private JLabel info(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(new Color(0x44,0x44,0x44));
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private String acortar(String s, int max) {
        if (s == null) return "--";
        return s.length() > max ? s.substring(0, max) + "…" : s;
    }
}
