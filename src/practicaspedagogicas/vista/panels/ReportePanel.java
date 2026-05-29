package practicaspedagogicas.vista.panels;

import practicaspedagogicas.util.ConexionDB;
import practicaspedagogicas.dao.ProgramaDAO;
import practicaspedagogicas.dao.PracticaDAO;
import practicaspedagogicas.modelo.Programa;
import practicaspedagogicas.modelo.Practica;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ReportePanel - Panel del Director para generar reportes consolidados:
 * cumplimiento de horas, evaluaciones por práctica y convenios por vencer.
 * Permite exportar a CSV para abrir en Excel.
 *
 * @version 1.0 - Oracle 10g XE
 */
public class ReportePanel extends JPanel {

    private static final Logger LOG = Logger.getLogger(ReportePanel.class.getName());
    private final ProgramaDAO programaDAO = new ProgramaDAO();
    private final PracticaDAO practicaDAO = new PracticaDAO();

    // ── Controles ─────────────────────────────────────────────────────────────
    private JComboBox<String>   cmbTipoReporte;
    private JComboBox<Programa> cmbPrograma;
    private JComboBox<Practica> cmbPractica;
    private JButton             btnGenerar, btnExportar;

    // ── Tabla resultados ──────────────────────────────────────────────────────
    private JTable            tablaResultados;
    private DefaultTableModel modeloResultados;
    private JLabel            lblTotal;

    private static final Color AZUL   = new Color(0x1F, 0x49, 0x7D);
    private static final Color AZUL_M = new Color(0x2E, 0x75, 0xB6);
    private static final Color VERDE  = new Color(0x27, 0xAE, 0x60);
    private static final Color BG     = new Color(0xF4, 0xF7, 0xFB);

    private static final String[] TIPOS_REPORTE = {
        "Cumplimiento de horas por práctica",
        "Evaluaciones finales por práctica",
        "Convenios próximos a vencer (30 días)",
        "Resumen general de estudiantes",
        "Actividades pendientes de validación"
    };

    public ReportePanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BG);
        setBorder(new EmptyBorder(14, 14, 14, 14));
        initUI();
        cargarCombos();
    }

    private void initUI() {
        // ── Título ─────────────────────────────────────────────────────────────
        JLabel lbl = new JLabel("Reportes y Dashboard – Director");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lbl.setForeground(AZUL);
        lbl.setBorder(new EmptyBorder(0,0,10,0));
        add(lbl, BorderLayout.NORTH);

        // ── Panel de controles ─────────────────────────────────────────────────
        JPanel panCtrl = new JPanel(new GridBagLayout());
        panCtrl.setBackground(Color.WHITE);
        panCtrl.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xCC,0xCC,0xCC)),
            new EmptyBorder(14, 14, 14, 14)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Tipo de reporte
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        panCtrl.add(etqCtrl("Tipo de reporte:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.5;
        cmbTipoReporte = new JComboBox<>(TIPOS_REPORTE);
        cmbTipoReporte.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panCtrl.add(cmbTipoReporte, gbc);

        // Programa
        gbc.gridx = 2; gbc.weightx = 0;
        panCtrl.add(etqCtrl("Programa:"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.4;
        cmbPrograma = new JComboBox<>();
        cmbPrograma.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panCtrl.add(cmbPrograma, gbc);

        // Práctica
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        panCtrl.add(etqCtrl("Práctica (opcional):"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.5;
        cmbPractica = new JComboBox<>();
        cmbPractica.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panCtrl.add(cmbPractica, gbc);

        // Botones
        gbc.gridx = 2; gbc.gridy = 1; gbc.weightx = 0;
        btnGenerar = btn("📊 Generar reporte", AZUL_M);
        panCtrl.add(btnGenerar, gbc);
        gbc.gridx = 3;
        btnExportar = btn("💾 Exportar CSV", VERDE);
        panCtrl.add(btnExportar, gbc);

        add(panCtrl, BorderLayout.NORTH);
        // Reposicionar: título al norte, controles en un sub-panel norte
        // Usamos un panel compuesto
        JPanel nPanel = new JPanel(new BorderLayout(0, 8));
        nPanel.setBackground(BG);
        nPanel.add(lbl, BorderLayout.NORTH);
        nPanel.add(panCtrl, BorderLayout.CENTER);
        add(nPanel, BorderLayout.NORTH);

        // ── Tabla de resultados ────────────────────────────────────────────────
        modeloResultados = new DefaultTableModel() {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaResultados = new JTable(modeloResultados);
        tablaResultados.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tablaResultados.setRowHeight(26);
        tablaResultados.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tablaResultados.getTableHeader().setBackground(AZUL);
        tablaResultados.getTableHeader().setForeground(Color.WHITE);
        tablaResultados.setSelectionBackground(new Color(0xD6,0xE4,0xF0));
        tablaResultados.setGridColor(new Color(0xE0,0xE0,0xE0));
        tablaResultados.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JScrollPane scroll = new JScrollPane(tablaResultados);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xC0,0xC0,0xC0)));
        add(scroll, BorderLayout.CENTER);

        // ── Barra de estado ────────────────────────────────────────────────────
        JPanel barraEst = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        barraEst.setBackground(BG);
        lblTotal = new JLabel("Registros: 0");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTotal.setForeground(AZUL);
        barraEst.add(lblTotal);
        add(barraEst, BorderLayout.SOUTH);

        // ── Eventos ────────────────────────────────────────────────────────────
        cmbPrograma.addActionListener(e -> cargarPracticasCombo());
        btnGenerar.addActionListener(e -> generarReporte());
        btnExportar.addActionListener(e -> exportarCSV());
    }

    // ── Carga de combos ───────────────────────────────────────────────────────

    private void cargarCombos() {
        cmbPrograma.removeAllItems();
        Programa todos = new Programa(); todos.setId(0); todos.setNombre("-- Todos los programas --");
        cmbPrograma.addItem(todos);
        programaDAO.listarTodos().forEach(cmbPrograma::addItem);
        cargarPracticasCombo();
    }

    private void cargarPracticasCombo() {
        cmbPractica.removeAllItems();
        Practica todas = new Practica(); todas.setId(0); todas.setNombre("-- Todas las prácticas --");
        cmbPractica.addItem(todas);
        Programa prog = (Programa) cmbPrograma.getSelectedItem();
        if (prog != null && prog.getId() > 0)
            practicaDAO.listarPorPrograma(prog.getId()).forEach(cmbPractica::addItem);
        else
            practicaDAO.listarTodos().forEach(cmbPractica::addItem);
    }

    // ── Generar reporte ───────────────────────────────────────────────────────

    private void generarReporte() {
        int tipo = cmbTipoReporte.getSelectedIndex();
        Programa prog = (Programa) cmbPrograma.getSelectedItem();
        Practica prac = (Practica) cmbPractica.getSelectedItem();
        int idProg = (prog != null && prog.getId() > 0) ? prog.getId() : -1;
        int idPrac = (prac != null && prac.getId() > 0) ? prac.getId() : -1;

        if      (tipo == 0) reporteHoras(idProg, idPrac);
        else if (tipo == 1) reporteEvaluaciones(idProg, idPrac);
        else if (tipo == 2) reporteConvenios();
        else if (tipo == 3) reporteResumenEstudiantes(idProg);
        else if (tipo == 4) reporteActividadesPendientes(idProg);
    }

    private void reporteHoras(int idProg, int idPrac) {
        String sql =
            "SELECT u.nombres || ' ' || u.apellidos AS estudiante, " +
            "u.numero_documento AS documento, " +
            "pr.nombre AS practica, pr.numero AS num_prac, " +
            "pr.horas_minimas AS horas_req, " +
            "FN_HORAS_CUMPLIDAS(ge.id) AS horas_ok, " +
            "FN_PORCENTAJE_HORAS(ge.id) AS porcentaje, " +
            "CASE WHEN FN_HORAS_CUMPLIDAS(ge.id) >= pr.horas_minimas " +
            "THEN 'CUMPLE' ELSE 'PENDIENTE' END AS estado_horas, " +
            "g.nombre AS grupo, p.nombre AS programa " +
            "FROM grupo_estudiante ge " +
            "JOIN usuario u    ON ge.id_estudiante = u.id " +
            "JOIN grupo g      ON ge.id_grupo      = g.id " +
            "JOIN practica pr  ON g.id_practica    = pr.id " +
            "JOIN programa p   ON pr.id_programa   = p.id " +
            "WHERE ge.estado <> 'Retirado' " +
            (idPrac > 0 ? "AND pr.id = " + idPrac + " " :
             idProg > 0 ? "AND p.id = " + idProg + " " : "") +
            "ORDER BY p.nombre, pr.numero, u.apellidos";
        ejecutarReporte(sql, new String[]{
            "Estudiante","Documento","Práctica","N°","Horas req.",
            "Horas aprobadas","% Cumplimiento","Estado","Grupo","Programa"});
    }

    private void reporteEvaluaciones(int idProg, int idPrac) {
        String sql =
            "SELECT u.nombres || ' ' || u.apellidos AS estudiante, " +
            "u.numero_documento, " +
            "pr.nombre AS practica, pr.numero, " +
            "ef.nota_definitiva, " +
            "CASE WHEN ef.aprobado = 1 THEN 'APROBADO' ELSE 'REPROBADO' END AS estado, " +
            "ef.fecha_evaluacion, " +
            "ud.nombres || ' ' || ud.apellidos AS docente_evaluador, " +
            "p.nombre AS programa " +
            "FROM evaluacion_final ef " +
            "JOIN grupo_estudiante ge ON ef.id_grupo_est = ge.id " +
            "JOIN usuario u   ON ge.id_estudiante = u.id " +
            "JOIN grupo g     ON ge.id_grupo      = g.id " +
            "JOIN practica pr ON g.id_practica    = pr.id " +
            "JOIN programa p  ON pr.id_programa   = p.id " +
            "JOIN usuario ud  ON ef.id_docente    = ud.id " +
            "WHERE 1=1 " +
            (idPrac > 0 ? "AND pr.id = " + idPrac + " " :
             idProg > 0 ? "AND p.id = " + idProg + " " : "") +
            "ORDER BY p.nombre, pr.numero, u.apellidos";
        ejecutarReporte(sql, new String[]{
            "Estudiante","Documento","Práctica","N°","Nota",
            "Estado","Fecha evaluación","Docente evaluador","Programa"});
    }

    private void reporteConvenios() {
        String sql =
            "SELECT c.numero_convenio, i.nombre AS institucion, " +
            "i.municipio, i.departamento, " +
            "c.fecha_firma, c.fecha_inicio, c.fecha_vencimiento, " +
            "c.estado, " +
            "ROUND(c.fecha_vencimiento - SYSDATE) AS dias_restantes, " +
            "p.nombre AS programa " +
            "FROM convenio c " +
            "JOIN institucion_receptora i ON c.id_institucion = i.id " +
            "JOIN programa p ON c.id_programa = p.id " +
            "WHERE c.estado = 'Vigente' AND c.fecha_vencimiento <= SYSDATE + 30 " +
            "ORDER BY c.fecha_vencimiento";
        ejecutarReporte(sql, new String[]{
            "N° Convenio","Institución","Municipio","Departamento",
            "Firma","Inicio","Vencimiento","Estado","Días restantes","Programa"});
    }

    private void reporteResumenEstudiantes(int idProg) {
        String sql =
            "SELECT u.nombres || ' ' || u.apellidos AS estudiante, " +
            "u.numero_documento, u.correo, " +
            "pr.nombre AS practica, pr.numero, " +
            "ge.estado AS estado_inscripcion, " +
            "FN_HORAS_CUMPLIDAS(ge.id) AS horas_ok, " +
            "pr.horas_minimas AS horas_req, " +
            "FN_PORCENTAJE_HORAS(ge.id) AS pct, " +
            "CASE WHEN ef.id IS NOT NULL " +
            "THEN TO_CHAR(ef.nota_definitiva) ELSE 'Sin evaluar' END AS nota, " +
            "p.nombre AS programa " +
            "FROM grupo_estudiante ge " +
            "JOIN usuario u    ON ge.id_estudiante = u.id " +
            "JOIN grupo g      ON ge.id_grupo      = g.id " +
            "JOIN practica pr  ON g.id_practica    = pr.id " +
            "JOIN programa p   ON pr.id_programa   = p.id " +
            "LEFT JOIN evaluacion_final ef ON ef.id_grupo_est = ge.id " +
            "WHERE ge.estado <> 'Retirado' " +
            (idProg > 0 ? "AND p.id = " + idProg + " " : "") +
            "ORDER BY p.nombre, pr.numero, u.apellidos";
        ejecutarReporte(sql, new String[]{
            "Estudiante","Documento","Correo","Práctica","N°",
            "Estado inscripción","Horas aprobadas","Horas req.","% Horas","Nota","Programa"});
    }

    private void reporteActividadesPendientes(int idProg) {
        String sql =
            "SELECT u.nombres || ' ' || u.apellidos AS estudiante, " +
            "ra.fecha_actividad, ra.tipo_actividad, " +
            "ra.horas_invertidas, ra.estado_validacion, " +
            "g.nombre AS grupo, pr.nombre AS practica, " +
            "p.nombre AS programa " +
            "FROM registro_actividad ra " +
            "JOIN grupo_estudiante ge ON ra.id_grupo_est = ge.id " +
            "JOIN usuario u   ON ge.id_estudiante = u.id " +
            "JOIN grupo g     ON ge.id_grupo      = g.id " +
            "JOIN practica pr ON g.id_practica    = pr.id " +
            "JOIN programa p  ON pr.id_programa   = p.id " +
            "WHERE ra.estado_validacion = 'Pendiente' " +
            (idProg > 0 ? "AND p.id = " + idProg + " " : "") +
            "ORDER BY p.nombre, pr.numero, ra.fecha_actividad";
        ejecutarReporte(sql, new String[]{
            "Estudiante","Fecha","Tipo","Horas","Estado","Grupo","Práctica","Programa"});
    }

    // ── Ejecución genérica ────────────────────────────────────────────────────

    private void ejecutarReporte(String sql, String[] columnas) {
        modeloResultados.setRowCount(0);
        modeloResultados.setColumnCount(0);
        for (String c : columnas) modeloResultados.addColumn(c);

        Connection con = null;
        int total = 0;
        try {
            con = ConexionDB.getConexion();
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                int cols = rs.getMetaData().getColumnCount();
                while (rs.next()) {
                    Object[] fila = new Object[cols];
                    for (int i = 0; i < cols; i++) fila[i] = rs.getObject(i + 1);
                    modeloResultados.addRow(fila);
                    total++;
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error generando reporte: " + e.getMessage(), e);
            alerta("Error al generar el reporte: " + e.getMessage());
        } finally { ConexionDB.liberar(con); }

        lblTotal.setText("Registros: " + total);
    }

    // ── Exportar CSV ──────────────────────────────────────────────────────────

    private void exportarCSV() {
        if (modeloResultados.getRowCount() == 0) {
            alerta("Primero genere un reporte para exportar."); return;
        }
        JFileChooser fc = new JFileChooser();
        String nombreArchivo = "reporte_practicas_"
            + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"))
            + ".csv";
        fc.setSelectedFile(new File(nombreArchivo));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File archivo = fc.getSelectedFile();
        if (!archivo.getName().endsWith(".csv"))
            archivo = new File(archivo.getAbsolutePath() + ".csv");

        try (PrintWriter pw = new PrintWriter(new FileWriter(archivo))) {
            // Encabezados
            StringBuilder sb = new StringBuilder();
            for (int c = 0; c < modeloResultados.getColumnCount(); c++) {
                if (c > 0) sb.append(";");
                sb.append(modeloResultados.getColumnName(c));
            }
            pw.println(sb);

            // Datos
            for (int r = 0; r < modeloResultados.getRowCount(); r++) {
                sb = new StringBuilder();
                for (int c = 0; c < modeloResultados.getColumnCount(); c++) {
                    if (c > 0) sb.append(";");
                    Object val = modeloResultados.getValueAt(r, c);
                    String s = val != null ? val.toString().replace(";", ",") : "";
                    sb.append("\"").append(s).append("\"");
                }
                pw.println(sb);
            }
            JOptionPane.showMessageDialog(this,
                "Archivo exportado exitosamente:\n" + archivo.getAbsolutePath(),
                "Exportación exitosa", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Error exportando CSV.", e);
            alerta("Error al guardar el archivo: " + e.getMessage());
        }
    }

    // ── Helpers UI ────────────────────────────────────────────────────────────
    private JLabel etqCtrl(String t) {
        JLabel l = new JLabel(t); l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(AZUL); return l;
    }
    private JButton btn(String t, Color c) {
        JButton b = new JButton(t); b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBackground(c); b.setForeground(Color.WHITE);
        b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return b;
    }
    private void alerta(String m) { JOptionPane.showMessageDialog(this, m, "Aviso", JOptionPane.WARNING_MESSAGE); }
}
