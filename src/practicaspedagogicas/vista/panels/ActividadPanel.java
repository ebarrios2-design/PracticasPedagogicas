package practicaspedagogicas.vista.panels;

import practicaspedagogicas.util.ConexionDB;
import practicaspedagogicas.util.SesionUsuario;
import practicaspedagogicas.modelo.Usuario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ActividadPanel - Panel del Estudiante para registrar actividades diarias,
 * cargar evidencias y consultar el estado de validación de horas.
 *
 * @version 1.0 - Oracle 10g XE
 */
public class ActividadPanel extends JPanel {

    private static final Logger LOG = Logger.getLogger(ActividadPanel.class.getName());
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Usuario usuarioActual = SesionUsuario.getInstance().getUsuario();

    // ── Tabla actividades ──────────────────────────────────────────────────────
    private JTable            tabla;
    private DefaultTableModel modelo;

    // ── Formulario ────────────────────────────────────────────────────────────
    private JTextField        txtFecha, txtHoras, txtEvidencia;
    private JComboBox<String> cmbTipo;
    private JTextArea         txtDescripcion;
    private JButton           btnRegistrar, btnNuevo;

    // ── Resumen horas ─────────────────────────────────────────────────────────
    private JLabel  lblHorasCumplidas, lblHorasRequeridas, lblPorcentaje;
    private JProgressBar progHoras;

    private static final Color AZUL   = new Color(0x1F, 0x49, 0x7D);
    private static final Color AZUL_M = new Color(0x2E, 0x75, 0xB6);
    private static final Color VERDE  = new Color(0x27, 0xAE, 0x60);
    private static final Color BG     = new Color(0xF4, 0xF7, 0xFB);

    public ActividadPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BG);
        setBorder(new EmptyBorder(14, 14, 14, 14));
        initUI();
        cargarActividades();
        actualizarResumenHoras();
    }

    private void initUI() {
        // ── Norte: título + resumen horas ──────────────────────────────────────
        JPanel norte = new JPanel(new BorderLayout(10, 6));
        norte.setBackground(BG);
        JLabel lbl = new JLabel("Mis Actividades de Práctica");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lbl.setForeground(AZUL);
        norte.add(lbl, BorderLayout.NORTH);

        // Card de resumen de horas
        JPanel cardHoras = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 6));
        cardHoras.setBackground(Color.WHITE);
        cardHoras.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xCC,0xCC,0xCC)),
            new EmptyBorder(8, 16, 8, 16)));

        lblHorasCumplidas  = resumenLbl("Horas aprobadas: 0");
        lblHorasRequeridas = resumenLbl("Horas requeridas: --");
        lblPorcentaje      = resumenLbl("Cumplimiento: 0%");
        progHoras = new JProgressBar(0, 100);
        progHoras.setPreferredSize(new Dimension(200, 18));
        progHoras.setStringPainted(true);
        progHoras.setForeground(VERDE);

        cardHoras.add(lblHorasCumplidas);
        cardHoras.add(lblHorasRequeridas);
        cardHoras.add(lblPorcentaje);
        cardHoras.add(progHoras);
        norte.add(cardHoras, BorderLayout.CENTER);
        add(norte, BorderLayout.NORTH);

        // ── Centro: tabla de actividades ──────────────────────────────────────
        JPanel centro = new JPanel(new BorderLayout(0, 4));
        centro.setBackground(BG);

        String[] cols = {"ID","Fecha","Tipo","Descripción","Horas","Estado","Observación Docente"};
        modelo = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = new JTable(modelo);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tabla.setRowHeight(24);
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        tabla.getTableHeader().setBackground(AZUL);
        tabla.getTableHeader().setForeground(Color.WHITE);
        tabla.setSelectionBackground(new Color(0xD6,0xE4,0xF0));
        tabla.setGridColor(new Color(0xE0,0xE0,0xE0));
        tabla.getColumnModel().getColumn(0).setMaxWidth(40);
        tabla.getColumnModel().getColumn(4).setMaxWidth(55);
        tabla.getColumnModel().getColumn(5).setMaxWidth(80);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xC0,0xC0,0xC0)));
        centro.add(scroll, BorderLayout.CENTER);

        JButton btnRefresh = btn("↺ Actualizar", AZUL_M);
        btnRefresh.addActionListener(e -> { cargarActividades(); actualizarResumenHoras(); });
        JPanel botC = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        botC.setBackground(BG); botC.add(btnRefresh);
        centro.add(botC, BorderLayout.SOUTH);
        add(centro, BorderLayout.CENTER);

        // ── Este: formulario nueva actividad ──────────────────────────────────
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xCC,0xCC,0xCC)),
            new EmptyBorder(14, 14, 14, 14)));
        form.setPreferredSize(new Dimension(280, 0));

        JLabel lblF = new JLabel("Registrar Actividad");
        lblF.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblF.setForeground(AZUL); lblF.setAlignmentX(LEFT_ALIGNMENT);
        form.add(lblF); form.add(Box.createVerticalStrut(10));

        txtFecha = campo(form, "Fecha (dd/MM/yyyy) *");
        txtFecha.setText(LocalDate.now().format(FMT));

        form.add(etq("Tipo de actividad *"));
        cmbTipo = new JComboBox<>(new String[]{
            "Observacion","Planeacion","Intervencion","Evaluacion","Reunion","Otro"});
        cmbTipo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmbTipo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        cmbTipo.setAlignmentX(LEFT_ALIGNMENT);
        form.add(cmbTipo); form.add(Box.createVerticalStrut(6));

        form.add(etq("Descripción detallada *"));
        txtDescripcion = new JTextArea(5, 16);
        txtDescripcion.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        txtDescripcion.setLineWrap(true); txtDescripcion.setWrapStyleWord(true);
        JScrollPane scrDesc = new JScrollPane(txtDescripcion);
        scrDesc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        scrDesc.setAlignmentX(LEFT_ALIGNMENT);
        form.add(scrDesc); form.add(Box.createVerticalStrut(6));

        txtHoras = campo(form, "Horas invertidas * (ej: 1.5)");

        txtEvidencia = campo(form, "Enlace de evidencia (opcional)");
        txtEvidencia.setToolTipText("URL de Google Drive, OneDrive, YouTube, etc.");

        form.add(Box.createVerticalStrut(12));
        btnNuevo    = btn("✏ Limpiar",         new Color(0x7F,0x8C,0x8D));
        btnRegistrar= btn("💾 Registrar actividad", VERDE);
        btnNuevo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        btnRegistrar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btnNuevo.setAlignmentX(LEFT_ALIGNMENT);
        btnRegistrar.setAlignmentX(LEFT_ALIGNMENT);
        form.add(btnNuevo); form.add(Box.createVerticalStrut(4)); form.add(btnRegistrar);
        add(form, BorderLayout.EAST);

        // ── Eventos ────────────────────────────────────────────────────────────
        btnNuevo.addActionListener(e -> limpiar());
        btnRegistrar.addActionListener(e -> registrar());
    }

    // ── Carga de datos desde Oracle ───────────────────────────────────────────

    private void cargarActividades() {
        modelo.setRowCount(0);
        if (usuarioActual == null) return;

        String sql =
            "SELECT ra.id, ra.fecha_actividad, ra.tipo_actividad, ra.descripcion, " +
            "ra.horas_invertidas, ra.estado_validacion, ra.observacion_doc " +
            "FROM registro_actividad ra " +
            "JOIN grupo_estudiante ge ON ra.id_grupo_est = ge.id " +
            "WHERE ge.id_estudiante = ? " +
            "ORDER BY ra.fecha_actividad DESC";

        Connection con = null;
        try {
            con = ConexionDB.getConexion();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, usuarioActual.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String desc = rs.getString("descripcion");
                        if (desc != null && desc.length() > 50) desc = desc.substring(0, 50) + "...";
                        String obs = rs.getString("observacion_doc");
                        modelo.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getDate("fecha_actividad"),
                            rs.getString("tipo_actividad"),
                            desc,
                            rs.getDouble("horas_invertidas"),
                            rs.getString("estado_validacion"),
                            obs != null ? obs : ""
                        });
                    }
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error cargando actividades: " + e.getMessage(), e);
        } finally {
            ConexionDB.liberar(con);
        }
    }

    private void actualizarResumenHoras() {
        if (usuarioActual == null) return;

        String sql =
            "SELECT " +
            "NVL(SUM(CASE WHEN ra.estado_validacion='Aprobado' THEN ra.horas_invertidas ELSE 0 END),0) AS horas_ok, " +
            "pr.horas_minimas " +
            "FROM grupo_estudiante ge " +
            "JOIN grupo g ON ge.id_grupo = g.id " +
            "JOIN practica pr ON g.id_practica = pr.id " +
            "LEFT JOIN registro_actividad ra ON ra.id_grupo_est = ge.id " +
            "WHERE ge.id_estudiante = ? AND ge.estado = 'Activo' AND pr.estado = 'Activa' " +
            "GROUP BY pr.horas_minimas";

        Connection con = null;
        try {
            con = ConexionDB.getConexion();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, usuarioActual.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        double cumpl  = rs.getDouble("horas_ok");
                        int    req    = rs.getInt("horas_minimas");
                        int    pct    = req > 0 ? (int) Math.min(100, (cumpl / req) * 100) : 0;
                        lblHorasCumplidas.setText(String.format("Horas aprobadas: %.1f", cumpl));
                        lblHorasRequeridas.setText("Horas requeridas: " + req);
                        lblPorcentaje.setText("Cumplimiento: " + pct + "%");
                        progHoras.setValue(pct);
                        progHoras.setString(pct + "%");
                        progHoras.setForeground(pct >= 100 ? VERDE : (pct >= 50 ? new Color(0xF3,0x9C,0x12) : AZUL_M));
                    } else {
                        lblHorasCumplidas.setText("Sin práctica activa");
                    }
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error actualizando resumen horas: " + e.getMessage(), e);
        } finally {
            ConexionDB.liberar(con);
        }
    }

    // ── Registrar actividad ───────────────────────────────────────────────────

    private void registrar() {
        String fechaStr = txtFecha.getText().trim();
        String desc     = txtDescripcion.getText().trim();
        String horasStr = txtHoras.getText().trim();

        if (fechaStr.isEmpty() || desc.isEmpty() || horasStr.isEmpty()) {
            alerta("Complete los campos obligatorios (*).");
            return;
        }

        LocalDate fecha;
        try { fecha = LocalDate.parse(fechaStr, FMT); }
        catch (DateTimeParseException e) { alerta("Formato de fecha incorrecto. Use dd/MM/yyyy."); return; }

        if (fecha.isAfter(LocalDate.now())) { alerta("La fecha no puede ser futura."); return; }

        double horas;
        try {
            horas = Double.parseDouble(horasStr);
            if (horas < 0.5 || horas > 12) throw new NumberFormatException();
        } catch (NumberFormatException e) { alerta("Las horas deben estar entre 0.5 y 12."); return; }

        // Obtener id_grupo_est del estudiante con práctica activa
        String sqlGE =
            "SELECT ge.id FROM grupo_estudiante ge " +
            "JOIN grupo g ON ge.id_grupo = g.id " +
            "JOIN practica pr ON g.id_practica = pr.id " +
            "WHERE ge.id_estudiante = ? AND ge.estado = 'Activo' AND pr.estado = 'Activa' " +
            "AND ROWNUM = 1";

        Connection con = null;
        try {
            con = ConexionDB.getConexion();
            con.setAutoCommit(false);
            int idGrupoEst = -1;

            try (PreparedStatement ps = con.prepareStatement(sqlGE)) {
                ps.setInt(1, usuarioActual.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) idGrupoEst = rs.getInt(1);
                }
            }

            if (idGrupoEst == -1) {
                alerta("No tiene una práctica activa asignada. Contacte al Coordinador.");
                con.rollback(); return;
            }

            // Llamar al procedimiento PR_REGISTRAR_ACTIVIDAD
            String call = "{call PR_REGISTRAR_ACTIVIDAD(?,?,?,?,?,?)}";
            try (CallableStatement cs = con.prepareCall(call)) {
                cs.setInt(1, idGrupoEst);
                cs.setString(2, (String) cmbTipo.getSelectedItem());
                cs.setString(3, desc);
                cs.setDouble(4, horas);
                cs.setDate(5, Date.valueOf(fecha));
                cs.registerOutParameter(6, Types.VARCHAR);
                cs.execute();
                String resultado = cs.getString(6);
                con.commit();

                if (resultado.startsWith("OK")) {
                    // Si hay enlace de evidencia, guardarlo
                    String enlace = txtEvidencia.getText().trim();
                    if (!enlace.isEmpty()) guardarEvidencia(idGrupoEst, enlace);
                    JOptionPane.showMessageDialog(this, resultado, "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    limpiar();
                    cargarActividades();
                    actualizarResumenHoras();
                } else {
                    JOptionPane.showMessageDialog(this, resultado, "Error", JOptionPane.ERROR_MESSAGE);
                    con.rollback();
                }
            }
        } catch (SQLException e) {
            try { if (con != null) con.rollback(); } catch (SQLException ex) {}
            LOG.log(Level.SEVERE, "Error registrando actividad: " + e.getMessage(), e);
            alerta("Error al registrar: " + e.getMessage());
        } finally {
            try { if (con != null) con.setAutoCommit(true); } catch (SQLException e) {}
            ConexionDB.liberar(con);
        }
    }

    private void guardarEvidencia(int idGrupoEst, String enlace) {
        // Primero obtener el último id de actividad insertado
        String sqlUltAct = "SELECT MAX(id) FROM registro_actividad WHERE id_grupo_est=?";
        String sqlEv = "INSERT INTO evidencia (id, id_actividad, tipo_evidencia, nombre_archivo, ruta_url) " +
                       "VALUES (SEQ_EVIDENCIA.NEXTVAL, ?, 'Enlace', 'Enlace de evidencia', ?)";
        Connection con = null;
        try {
            con = ConexionDB.getConexion();
            int idAct = -1;
            try (PreparedStatement ps = con.prepareStatement(sqlUltAct)) {
                ps.setInt(1, idGrupoEst);
                try (ResultSet rs = ps.executeQuery()) { if (rs.next()) idAct = rs.getInt(1); }
            }
            if (idAct > 0) {
                try (PreparedStatement ps = con.prepareStatement(sqlEv)) {
                    ps.setInt(1, idAct); ps.setString(2, enlace);
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Error guardando evidencia.", e);
        } finally { ConexionDB.liberar(con); }
    }

    private void limpiar() {
        txtFecha.setText(LocalDate.now().format(FMT));
        cmbTipo.setSelectedIndex(0);
        txtDescripcion.setText("");
        txtHoras.setText("");
        txtEvidencia.setText("");
    }

    // ── Helpers UI ────────────────────────────────────────────────────────────
    private JLabel resumenLbl(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(AZUL); return l;
    }
    private JLabel etq(String t) {
        JLabel l = new JLabel(t); l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(AZUL); l.setAlignmentX(LEFT_ALIGNMENT); return l;
    }
    private JTextField campo(JPanel p, String e) {
        p.add(etq(e));
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        tf.setAlignmentX(LEFT_ALIGNMENT);
        p.add(tf); p.add(Box.createVerticalStrut(5)); return tf;
    }
    private JButton btn(String t, Color c) {
        JButton b = new JButton(t); b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBackground(c); b.setForeground(Color.WHITE);
        b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return b;
    }
    private void alerta(String m) { JOptionPane.showMessageDialog(this, m, "Aviso", JOptionPane.WARNING_MESSAGE); }
}
