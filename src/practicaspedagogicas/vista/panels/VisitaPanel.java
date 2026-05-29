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
 * VisitaPanel - Panel del Docente Asesor para registrar visitas pedagógicas
 * y validar las horas de actividades de sus estudiantes.
 *
 * @version 1.0 - Oracle 10g XE
 */
public class VisitaPanel extends JPanel {

    private static final Logger LOG = Logger.getLogger(VisitaPanel.class.getName());
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Usuario docente = SesionUsuario.getInstance().getUsuario();

    // ── Tabs principales ──────────────────────────────────────────────────────
    private JTabbedPane tabs;

    // ── Tab 1: Visitas ────────────────────────────────────────────────────────
    private JTable            tablaVisitas;
    private DefaultTableModel modeloVisitas;
    private JComboBox<Object> cmbGrupoVisita;
    private JTextField        txtFechaVisita;
    private JComboBox<String> cmbTipoVisita;
    private JTextArea         txtObjetivo, txtObservaciones, txtRecomendaciones;
    private JButton           btnRegistrarVisita, btnNuevaVisita;

    // ── Tab 2: Validar horas ───────────────────────────────────────────────────
    private JTable            tablaActividades;
    private DefaultTableModel modeloActividades;
    private JComboBox<Object> cmbGrupoHoras;
    private JTextArea         txtObservacionVal;
    private JButton           btnAprobar, btnRechazar, btnCargarActs;

    private static final Color AZUL   = new Color(0x1F, 0x49, 0x7D);
    private static final Color AZUL_M = new Color(0x2E, 0x75, 0xB6);
    private static final Color VERDE  = new Color(0x27, 0xAE, 0x60);
    private static final Color ROJO   = new Color(0xC0, 0x39, 0x2B);
    private static final Color BG     = new Color(0xF4, 0xF7, 0xFB);

    public VisitaPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BG);
        setBorder(new EmptyBorder(14, 14, 14, 14));
        initUI();
        cargarGruposDocente();
        cargarVisitas();
    }

    private void initUI() {
        JLabel lbl = new JLabel("Visitas Pedagógicas y Validación de Horas");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lbl.setForeground(AZUL);
        lbl.setBorder(new EmptyBorder(0,0,10,0));
        add(lbl, BorderLayout.NORTH);

        tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabs.addTab("🗓 Registrar Visita",    buildTabVisita());
        tabs.addTab("✅ Validar Horas",        buildTabValidar());
        add(tabs, BorderLayout.CENTER);
    }

    // ── Tab 1: Registrar visita ───────────────────────────────────────────────

    private JPanel buildTabVisita() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBackground(BG); p.setBorder(new EmptyBorder(10, 0, 0, 0));

        // Tabla historial
        String[] cols = {"ID","Fecha","Grupo","Tipo","Objetivo","Estado"};
        modeloVisitas = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaVisitas = new JTable(modeloVisitas);
        estTabla(tablaVisitas);
        tablaVisitas.getColumnModel().getColumn(0).setMaxWidth(40);
        JScrollPane scroll = new JScrollPane(tablaVisitas);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xC0,0xC0,0xC0)));
        p.add(scroll, BorderLayout.CENTER);

        // Formulario nueva visita
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xCC,0xCC,0xCC)),
            new EmptyBorder(14,14,14,14)));
        form.setPreferredSize(new Dimension(300, 0));

        JLabel lblF = new JLabel("Nueva Visita Pedagógica");
        lblF.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblF.setForeground(AZUL); lblF.setAlignmentX(LEFT_ALIGNMENT);
        form.add(lblF); form.add(Box.createVerticalStrut(10));

        form.add(etq("Grupo *"));
        cmbGrupoVisita = new JComboBox<>();
        estCmb(cmbGrupoVisita); form.add(cmbGrupoVisita);
        form.add(Box.createVerticalStrut(6));

        txtFechaVisita = campo(form, "Fecha (dd/MM/yyyy) *");
        txtFechaVisita.setText(LocalDate.now().format(FMT));

        form.add(etq("Tipo de visita *"));
        cmbTipoVisita = new JComboBox<>(new String[]{"Presencial","Virtual"});
        cmbTipoVisita.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmbTipoVisita.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        cmbTipoVisita.setAlignmentX(LEFT_ALIGNMENT);
        form.add(cmbTipoVisita); form.add(Box.createVerticalStrut(6));

        txtObjetivo = area(form, "Objetivo de la visita *", 3);
        txtObservaciones = area(form, "Observaciones pedagógicas *", 4);
        txtRecomendaciones = area(form, "Recomendaciones (opcional)", 3);

        form.add(Box.createVerticalStrut(10));
        btnNuevaVisita    = btn("✏ Limpiar", new Color(0x7F,0x8C,0x8D));
        btnRegistrarVisita= btn("💾 Registrar visita", VERDE);
        btnNuevaVisita.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        btnRegistrarVisita.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btnNuevaVisita.setAlignmentX(LEFT_ALIGNMENT);
        btnRegistrarVisita.setAlignmentX(LEFT_ALIGNMENT);
        form.add(btnNuevaVisita);
        form.add(Box.createVerticalStrut(4));
        form.add(btnRegistrarVisita);
        p.add(form, BorderLayout.EAST);

        btnNuevaVisita.addActionListener(e -> limpiarVisita());
        btnRegistrarVisita.addActionListener(e -> registrarVisita());
        return p;
    }

    // ── Tab 2: Validar horas ──────────────────────────────────────────────────

    private JPanel buildTabValidar() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBackground(BG); p.setBorder(new EmptyBorder(10,0,0,0));

        // Barra filtro
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        barra.setBackground(BG);
        barra.add(new JLabel("Grupo:"));
        cmbGrupoHoras = new JComboBox<>();
        cmbGrupoHoras.setPreferredSize(new Dimension(220, 28));
        barra.add(cmbGrupoHoras);
        btnCargarActs = btn("🔍 Cargar pendientes", AZUL_M);
        barra.add(btnCargarActs);
        p.add(barra, BorderLayout.NORTH);

        // Tabla actividades pendientes
        String[] cols = {"ID Act","Estudiante","Fecha","Tipo","Descripción","Horas","Estado"};
        modeloActividades = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaActividades = new JTable(modeloActividades);
        estTabla(tablaActividades);
        tablaActividades.getColumnModel().getColumn(0).setMaxWidth(55);
        tablaActividades.getColumnModel().getColumn(5).setMaxWidth(50);
        tablaActividades.getColumnModel().getColumn(6).setMaxWidth(80);
        p.add(new JScrollPane(tablaActividades), BorderLayout.CENTER);

        // Panel validación
        JPanel panVal = new JPanel(new BorderLayout(6, 6));
        panVal.setBackground(Color.WHITE);
        panVal.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xCC,0xCC,0xCC)),
            new EmptyBorder(10,10,10,10)));
        panVal.setPreferredSize(new Dimension(0, 120));

        JLabel lblObs = new JLabel("Observación (obligatoria al rechazar):");
        lblObs.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblObs.setForeground(AZUL);
        panVal.add(lblObs, BorderLayout.NORTH);

        txtObservacionVal = new JTextArea(2, 20);
        txtObservacionVal.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        txtObservacionVal.setLineWrap(true);
        panVal.add(new JScrollPane(txtObservacionVal), BorderLayout.CENTER);

        JPanel btnVal = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnVal.setBackground(Color.WHITE);
        btnAprobar = btn("✅ Aprobar horas",  VERDE);
        btnRechazar= btn("❌ Rechazar horas", ROJO);
        btnVal.add(btnAprobar); btnVal.add(btnRechazar);
        panVal.add(btnVal, BorderLayout.SOUTH);
        p.add(panVal, BorderLayout.SOUTH);

        btnCargarActs.addActionListener(e -> cargarActividadesPendientes());
        btnAprobar.addActionListener(e -> validarActividad("Aprobado"));
        btnRechazar.addActionListener(e -> validarActividad("Rechazado"));
        return p;
    }

    // ── Lógica ────────────────────────────────────────────────────────────────

    private void cargarGruposDocente() {
        if (docente == null) return;
        String sql =
            "SELECT g.id, g.nombre || ' – ' || pr.nombre AS etiqueta " +
            "FROM grupo_docente gd " +
            "JOIN grupo g ON gd.id_grupo = g.id " +
            "JOIN practica pr ON g.id_practica = pr.id " +
            "WHERE gd.id_docente = ? AND g.activo = 1 ORDER BY g.nombre";

        Connection con = null;
        try {
            con = ConexionDB.getConexion();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, docente.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    cmbGrupoVisita.removeAllItems();
                    cmbGrupoHoras.removeAllItems();
                    while (rs.next()) {
                        String item = rs.getInt("id") + " – " + rs.getString("etiqueta");
                        cmbGrupoVisita.addItem(item);
                        cmbGrupoHoras.addItem(item);
                    }
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error cargando grupos del docente.", e);
        } finally { ConexionDB.liberar(con); }
    }

    private void cargarVisitas() {
        modeloVisitas.setRowCount(0);
        if (docente == null) return;
        String sql =
            "SELECT v.id, v.fecha_visita, g.nombre AS grupo, v.tipo_visita, v.objetivo " +
            "FROM visita_docente v JOIN grupo g ON v.id_grupo = g.id " +
            "WHERE v.id_docente = ? ORDER BY v.fecha_visita DESC";
        Connection con = null;
        try {
            con = ConexionDB.getConexion();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, docente.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String obj = rs.getString("objetivo");
                        if (obj != null && obj.length() > 40) obj = obj.substring(0,40) + "...";
                        modeloVisitas.addRow(new Object[]{
                            rs.getInt("id"), rs.getDate("fecha_visita"),
                            rs.getString("grupo"), rs.getString("tipo_visita"), obj, "Registrada"
                        });
                    }
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error cargando visitas.", e);
        } finally { ConexionDB.liberar(con); }
    }

    private void registrarVisita() {
        Object grupoItem = cmbGrupoVisita.getSelectedItem();
        if (grupoItem == null) { alerta("Seleccione un grupo."); return; }
        String fechaStr = txtFechaVisita.getText().trim();
        String obj      = txtObjetivo.getText().trim();
        String obs      = txtObservaciones.getText().trim();

        if (fechaStr.isEmpty() || obj.isEmpty() || obs.isEmpty()) {
            alerta("Complete los campos obligatorios (*)."); return;
        }
        LocalDate fecha;
        try { fecha = LocalDate.parse(fechaStr, FMT); }
        catch (DateTimeParseException e) { alerta("Formato de fecha incorrecto. Use dd/MM/yyyy."); return; }

        int idGrupo;
        try { idGrupo = Integer.parseInt(grupoItem.toString().split(" – ")[0].trim()); }
        catch (Exception e) { alerta("Error obteniendo el grupo seleccionado."); return; }

        String sql =
            "INSERT INTO visita_docente (id, id_grupo, id_docente, fecha_visita, " +
            "tipo_visita, objetivo, observaciones, recomendaciones) " +
            "VALUES (SEQ_VISITA.NEXTVAL, ?, ?, ?, ?, ?, ?, ?)";

        Connection con = null;
        try {
            con = ConexionDB.getConexion(); con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idGrupo);
                ps.setInt(2, docente.getId());
                ps.setDate(3, Date.valueOf(fecha));
                ps.setString(4, (String) cmbTipoVisita.getSelectedItem());
                ps.setString(5, obj);
                ps.setString(6, obs);
                ps.setString(7, txtRecomendaciones.getText().trim());
                ps.executeUpdate();
            }
            con.commit();
            JOptionPane.showMessageDialog(this, "Visita registrada exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            limpiarVisita(); cargarVisitas();
        } catch (SQLException e) {
            try { if (con!=null) con.rollback(); } catch (SQLException ex) {}
            LOG.log(Level.SEVERE, "Error registrando visita: " + e.getMessage(), e);
            alerta("Error al registrar la visita: " + e.getMessage());
        } finally {
            try { if (con!=null) con.setAutoCommit(true); } catch (SQLException e) {}
            ConexionDB.liberar(con);
        }
    }

    private void cargarActividadesPendientes() {
        modeloActividades.setRowCount(0);
        Object grupoItem = cmbGrupoHoras.getSelectedItem();
        if (grupoItem == null) return;
        int idGrupo;
        try { idGrupo = Integer.parseInt(grupoItem.toString().split(" – ")[0].trim()); }
        catch (Exception e) { return; }

        String sql =
            "SELECT ra.id, u.nombres || ' ' || u.apellidos AS estudiante, " +
            "ra.fecha_actividad, ra.tipo_actividad, ra.descripcion, " +
            "ra.horas_invertidas, ra.estado_validacion " +
            "FROM registro_actividad ra " +
            "JOIN grupo_estudiante ge ON ra.id_grupo_est = ge.id " +
            "JOIN usuario u ON ge.id_estudiante = u.id " +
            "WHERE ge.id_grupo = ? AND ra.estado_validacion = 'Pendiente' " +
            "ORDER BY ra.fecha_actividad";

        Connection con = null;
        try {
            con = ConexionDB.getConexion();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idGrupo);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String desc = rs.getString("descripcion");
                        if (desc != null && desc.length() > 45) desc = desc.substring(0,45) + "...";
                        modeloActividades.addRow(new Object[]{
                            rs.getInt("id"), rs.getString("estudiante"),
                            rs.getDate("fecha_actividad"), rs.getString("tipo_actividad"),
                            desc, rs.getDouble("horas_invertidas"), rs.getString("estado_validacion")
                        });
                    }
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error cargando actividades pendientes.", e);
        } finally { ConexionDB.liberar(con); }
    }

    private void validarActividad(String nuevoEstado) {
        int f = tablaActividades.getSelectedRow();
        if (f < 0) { alerta("Seleccione una actividad de la tabla."); return; }
        String obs = txtObservacionVal.getText().trim();
        if ("Rechazado".equals(nuevoEstado) && obs.isEmpty()) {
            alerta("Debe escribir una observación al rechazar una actividad."); return;
        }
        int idActividad = (int) modeloActividades.getValueAt(f, 0);
        String call = "{call PR_VALIDAR_ACTIVIDAD(?,?,?,?,?)}";
        Connection con = null;
        try {
            con = ConexionDB.getConexion(); con.setAutoCommit(false);
            try (CallableStatement cs = con.prepareCall(call)) {
                cs.setInt(1, idActividad);
                cs.setInt(2, docente.getId());
                cs.setString(3, nuevoEstado);
                cs.setString(4, obs.isEmpty() ? "Revisado por docente asesor." : obs);
                cs.registerOutParameter(5, Types.VARCHAR);
                cs.execute();
                String resultado = cs.getString(5);
                con.commit();
                if (resultado.startsWith("OK")) {
                    JOptionPane.showMessageDialog(this, resultado, "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    txtObservacionVal.setText("");
                    cargarActividadesPendientes();
                } else {
                    JOptionPane.showMessageDialog(this, resultado, "Error", JOptionPane.ERROR_MESSAGE);
                    con.rollback();
                }
            }
        } catch (SQLException e) {
            try { if (con!=null) con.rollback(); } catch (SQLException ex) {}
            LOG.log(Level.SEVERE, "Error validando actividad.", e);
            alerta("Error: " + e.getMessage());
        } finally {
            try { if (con!=null) con.setAutoCommit(true); } catch (SQLException e) {}
            ConexionDB.liberar(con);
        }
    }

    private void limpiarVisita() {
        txtFechaVisita.setText(LocalDate.now().format(FMT));
        cmbTipoVisita.setSelectedIndex(0);
        txtObjetivo.setText(""); txtObservaciones.setText(""); txtRecomendaciones.setText("");
    }

    // ── Helpers UI ────────────────────────────────────────────────────────────
    private JLabel etq(String t) {
        JLabel l = new JLabel(t); l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(AZUL); l.setAlignmentX(LEFT_ALIGNMENT); return l;
    }
    private JTextField campo(JPanel p, String e) {
        p.add(etq(e)); JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        tf.setAlignmentX(LEFT_ALIGNMENT);
        p.add(tf); p.add(Box.createVerticalStrut(5)); return tf;
    }
    private JTextArea area(JPanel p, String etq, int rows) {
        p.add(etq(etq));
        JTextArea ta = new JTextArea(rows, 16);
        ta.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        ta.setLineWrap(true); ta.setWrapStyleWord(true);
        JScrollPane sc = new JScrollPane(ta);
        sc.setMaximumSize(new Dimension(Integer.MAX_VALUE, rows * 22 + 10));
        sc.setAlignmentX(LEFT_ALIGNMENT);
        p.add(sc); p.add(Box.createVerticalStrut(5)); return ta;
    }
    private void estCmb(JComboBox<?> c) {
        c.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        c.setAlignmentX(LEFT_ALIGNMENT);
    }
    private JButton btn(String t, Color c) {
        JButton b = new JButton(t); b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBackground(c); b.setForeground(Color.WHITE);
        b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return b;
    }
    private void estTabla(JTable t) {
        t.setFont(new Font("Segoe UI", Font.PLAIN, 11)); t.setRowHeight(24);
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        t.getTableHeader().setBackground(AZUL); t.getTableHeader().setForeground(Color.WHITE);
        t.setSelectionBackground(new Color(0xD6,0xE4,0xF0));
        t.setGridColor(new Color(0xE0,0xE0,0xE0));
    }
    private void alerta(String m) { JOptionPane.showMessageDialog(this, m, "Aviso", JOptionPane.WARNING_MESSAGE); }
}
