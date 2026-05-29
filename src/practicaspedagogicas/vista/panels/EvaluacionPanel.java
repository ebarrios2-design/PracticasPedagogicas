package practicaspedagogicas.vista.panels;

import practicaspedagogicas.util.ConexionDB;
import practicaspedagogicas.util.SesionUsuario;
import practicaspedagogicas.modelo.Usuario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * EvaluacionPanel - Panel del Docente Asesor para registrar evaluaciones
 * finales con rúbrica parametrizable y cálculo automático de nota.
 *
 * @version 1.0 - Oracle 10g XE
 */
public class EvaluacionPanel extends JPanel {

    private static final Logger LOG = Logger.getLogger(EvaluacionPanel.class.getName());
    private final Usuario docente = SesionUsuario.getInstance().getUsuario();

    // ── Tab: lista estudiantes ─────────────────────────────────────────────────
    private JTable            tablaEstudiantes;
    private DefaultTableModel modeloEstudiantes;
    private JComboBox<Object> cmbGrupo;

    // ── Panel de rúbrica ──────────────────────────────────────────────────────
    private JPanel         panelRubrica;
    private JComboBox<Object> cmbRubrica;
    private JTextArea      txtObservacionFinal;
    private JLabel         lblNotaCalculada, lblNotaMinima;
    private JTextField     txtNotaDefinitiva;
    private JButton        btnEvaluar, btnCargar;

    // Criterios de la rúbrica (dinámicos)
    private final List<CriterioFila> criterioFilas = new ArrayList<>();

    // IDs seleccionados
    private int idGrupoEstSeleccionado = -1;
    private int idRubricaSeleccionada  = -1;

    private static final Color AZUL   = new Color(0x1F, 0x49, 0x7D);
    private static final Color AZUL_M = new Color(0x2E, 0x75, 0xB6);
    private static final Color VERDE  = new Color(0x27, 0xAE, 0x60);
    private static final Color ROJO   = new Color(0xC0, 0x39, 0x2B);
    private static final Color AMARI  = new Color(0xF3, 0x9C, 0x12);
    private static final Color BG     = new Color(0xF4, 0xF7, 0xFB);

    // ── Clase interna para cada fila de criterio ──────────────────────────────
    private static class CriterioFila {
        int    idCriterio;
        String nombre;
        double ponderacion;
        double valExcelente, valBueno, valAceptable, valDeficiente;
        JLabel  lblNombre, lblPonderacion;
        JComboBox<String> cmbNivel;

        CriterioFila(int id, String nombre, double pond,
                     double vE, double vB, double vA, double vD) {
            this.idCriterio    = id;
            this.nombre        = nombre;
            this.ponderacion   = pond;
            this.valExcelente  = vE;
            this.valBueno      = vB;
            this.valAceptable  = vA;
            this.valDeficiente = vD;
            lblNombre      = new JLabel(nombre);
            lblNombre.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblPonderacion = new JLabel(pond + "%");
            lblPonderacion.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblPonderacion.setForeground(new Color(0x1F,0x49,0x7D));
            cmbNivel = new JComboBox<>(new String[]{
                "Excelente (" + vE + ")",
                "Bueno ("     + vB + ")",
                "Aceptable (" + vA + ")",
                "Deficiente (" + vD + ")"
            });
            cmbNivel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        }

        double getValorSeleccionado() {
            int idx = cmbNivel.getSelectedIndex();
            if (idx == 0) return valExcelente;
            if (idx == 1) return valBueno;
            if (idx == 2) return valAceptable;
            return valDeficiente;
        }

        String getNivelTexto() {
            int idx = cmbNivel.getSelectedIndex();
            if (idx == 0) return "Excelente";
            if (idx == 1) return "Bueno";
            if (idx == 2) return "Aceptable";
            return "Deficiente";
        }
    }

    public EvaluacionPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BG);
        setBorder(new EmptyBorder(14, 14, 14, 14));
        initUI();
        cargarGruposDocente();
    }

    private void initUI() {
        JLabel lbl = new JLabel("Evaluación Final de Estudiantes");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lbl.setForeground(AZUL);
        lbl.setBorder(new EmptyBorder(0,0,10,0));
        add(lbl, BorderLayout.NORTH);

        // ── Split: izquierda (lista estudiantes) | derecha (rúbrica) ──────────
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setDividerLocation(420);
        split.setBorder(null);

        // ── Panel izquierdo: seleccionar grupo y estudiante ────────────────────
        JPanel izq = new JPanel(new BorderLayout(6, 6));
        izq.setBackground(BG);

        JPanel barraGrupo = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        barraGrupo.setBackground(BG);
        barraGrupo.add(new JLabel("Grupo:"));
        cmbGrupo = new JComboBox<>();
        cmbGrupo.setPreferredSize(new Dimension(240, 28));
        barraGrupo.add(cmbGrupo);
        btnCargar = btn("🔍 Cargar estudiantes", AZUL_M);
        barraGrupo.add(btnCargar);
        izq.add(barraGrupo, BorderLayout.NORTH);

        String[] colsE = {"ID GE","Estudiante","Documento","Horas %","Evaluado"};
        modeloEstudiantes = new DefaultTableModel(colsE, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaEstudiantes = new JTable(modeloEstudiantes);
        tablaEstudiantes.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tablaEstudiantes.setRowHeight(26);
        tablaEstudiantes.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tablaEstudiantes.getTableHeader().setBackground(AZUL);
        tablaEstudiantes.getTableHeader().setForeground(Color.WHITE);
        tablaEstudiantes.setSelectionBackground(new Color(0xD6,0xE4,0xF0));
        tablaEstudiantes.setGridColor(new Color(0xE0,0xE0,0xE0));
        tablaEstudiantes.getColumnModel().getColumn(0).setMaxWidth(50);
        tablaEstudiantes.getColumnModel().getColumn(3).setMaxWidth(60);
        tablaEstudiantes.getColumnModel().getColumn(4).setMaxWidth(70);

        izq.add(new JScrollPane(tablaEstudiantes), BorderLayout.CENTER);
        split.setLeftComponent(izq);

        // ── Panel derecho: rúbrica y calificación ─────────────────────────────
        panelRubrica = new JPanel(new BorderLayout(6, 6));
        panelRubrica.setBackground(Color.WHITE);
        panelRubrica.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xCC,0xCC,0xCC)),
            new EmptyBorder(14, 14, 14, 14)));

        // Selector de rúbrica
        JPanel topRub = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        topRub.setBackground(Color.WHITE);
        topRub.add(new JLabel("Rúbrica:"));
        cmbRubrica = new JComboBox<>();
        cmbRubrica.setPreferredSize(new Dimension(220, 26));
        topRub.add(cmbRubrica);
        JButton btnCargarRub = btn("📋 Cargar criterios", AZUL_M);
        topRub.add(btnCargarRub);
        panelRubrica.add(topRub, BorderLayout.NORTH);

        // Área de criterios (dinámica, se rellena al cargar la rúbrica)
        JScrollPane scrRub = new JScrollPane();
        scrRub.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0xCC,0xCC,0xCC)),
            "Criterios de evaluación",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 11), AZUL));
        panelRubrica.add(scrRub, BorderLayout.CENTER);

        // Panel inferior: nota + observación + botón guardar
        JPanel botRub = new JPanel();
        botRub.setLayout(new BoxLayout(botRub, BoxLayout.Y_AXIS));
        botRub.setBackground(Color.WHITE);
        botRub.setBorder(new EmptyBorder(10, 0, 0, 0));

        // Notas
        JPanel panNotas = new JPanel(new GridLayout(3, 2, 6, 4));
        panNotas.setBackground(Color.WHITE);
        panNotas.add(new JLabel("Nota calculada (automática):"));
        lblNotaCalculada = new JLabel("0.00");
        lblNotaCalculada.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblNotaCalculada.setForeground(AZUL_M);
        panNotas.add(lblNotaCalculada);
        panNotas.add(new JLabel("Nota mínima aprobatoria:"));
        lblNotaMinima = new JLabel("--");
        lblNotaMinima.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblNotaMinima.setForeground(ROJO);
        panNotas.add(lblNotaMinima);
        panNotas.add(new JLabel("Nota definitiva (puede ajustar):"));
        txtNotaDefinitiva = new JTextField("0.00");
        txtNotaDefinitiva.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panNotas.add(txtNotaDefinitiva);
        botRub.add(panNotas);
        botRub.add(Box.createVerticalStrut(8));

        // Observación general
        JLabel lblObs = new JLabel("Observación general (obligatoria):");
        lblObs.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblObs.setForeground(AZUL); lblObs.setAlignmentX(LEFT_ALIGNMENT);
        botRub.add(lblObs);
        txtObservacionFinal = new JTextArea(3, 20);
        txtObservacionFinal.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        txtObservacionFinal.setLineWrap(true);
        JScrollPane scrObs = new JScrollPane(txtObservacionFinal);
        scrObs.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));
        scrObs.setAlignmentX(LEFT_ALIGNMENT);
        botRub.add(scrObs);
        botRub.add(Box.createVerticalStrut(8));

        btnEvaluar = btn("💾 Registrar evaluación final", VERDE);
        btnEvaluar.setAlignmentX(LEFT_ALIGNMENT);
        btnEvaluar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        botRub.add(btnEvaluar);
        panelRubrica.add(botRub, BorderLayout.SOUTH);
        split.setRightComponent(panelRubrica);
        add(split, BorderLayout.CENTER);

        // ── Eventos ────────────────────────────────────────────────────────────
        btnCargar.addActionListener(e -> cargarEstudiantesGrupo());
        btnCargarRub.addActionListener(e -> cargarCriteriosRubrica(scrRub));
        btnEvaluar.addActionListener(e -> registrarEvaluacion());

        tablaEstudiantes.getSelectionModel().addListSelectionListener(ev -> {
            if (!ev.getValueIsAdjusting()) seleccionarEstudiante();
        });

        // Recalcular nota al cambiar nivel de cualquier criterio
        cmbRubrica.addActionListener(e -> recalcularNota());
    }

    // ── Carga de datos ────────────────────────────────────────────────────────

    private void cargarGruposDocente() {
        if (docente == null) return;
        String sql =
            "SELECT g.id, g.nombre || ' – ' || pr.nombre AS etiqueta " +
            "FROM grupo_docente gd " +
            "JOIN grupo g    ON gd.id_grupo    = g.id " +
            "JOIN practica pr ON g.id_practica = pr.id " +
            "WHERE gd.id_docente = ? AND g.activo = 1 ORDER BY g.nombre";
        Connection con = null;
        try {
            con = ConexionDB.getConexion();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, docente.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    cmbGrupo.removeAllItems();
                    while (rs.next())
                        cmbGrupo.addItem(rs.getInt("id") + " – " + rs.getString("etiqueta"));
                }
            }
            cargarRubricas();
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error cargando grupos.", e);
        } finally { ConexionDB.liberar(con); }
    }

    private void cargarRubricas() {
        if (docente == null) return;
        String sql =
            "SELECT r.id, r.nombre, r.escala_min FROM rubrica r " +
            "JOIN practica pr ON r.id_practica = pr.id " +
            "JOIN grupo g ON g.id_practica = pr.id " +
            "JOIN grupo_docente gd ON gd.id_grupo = g.id " +
            "WHERE gd.id_docente = ? AND r.activo = 1 " +
            "GROUP BY r.id, r.nombre, r.escala_min ORDER BY r.nombre";
        Connection con = null;
        try {
            con = ConexionDB.getConexion();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, docente.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    cmbRubrica.removeAllItems();
                    while (rs.next())
                        cmbRubrica.addItem(rs.getInt("id") + " | " + rs.getString("nombre")
                            + " (min: " + rs.getDouble("escala_min") + ")");
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error cargando rúbricas.", e);
        } finally { ConexionDB.liberar(con); }
    }

    private void cargarEstudiantesGrupo() {
        modeloEstudiantes.setRowCount(0);
        Object grupoItem = cmbGrupo.getSelectedItem();
        if (grupoItem == null) return;
        int idGrupo;
        try { idGrupo = Integer.parseInt(grupoItem.toString().split(" – ")[0].trim()); }
        catch (Exception e) { return; }

        String sql =
            "SELECT ge.id AS id_ge, u.nombres || ' ' || u.apellidos AS nombre, " +
            "u.numero_documento, " +
            "ROUND((FN_HORAS_CUMPLIDAS(ge.id) / pr.horas_minimas) * 100, 1) AS pct_horas, " +
            "CASE WHEN ef.id IS NOT NULL THEN 'Sí (' || ef.nota_definitiva || ')' ELSE 'No' END AS evaluado " +
            "FROM grupo_estudiante ge " +
            "JOIN usuario u   ON ge.id_estudiante = u.id " +
            "JOIN grupo g     ON ge.id_grupo      = g.id " +
            "JOIN practica pr ON g.id_practica    = pr.id " +
            "LEFT JOIN evaluacion_final ef ON ef.id_grupo_est = ge.id " +
            "WHERE ge.id_grupo = ? AND ge.estado <> 'Retirado' " +
            "ORDER BY u.apellidos";

        Connection con = null;
        try {
            con = ConexionDB.getConexion();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idGrupo);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        double pct = rs.getDouble("pct_horas");
                        modeloEstudiantes.addRow(new Object[]{
                            rs.getInt("id_ge"),
                            rs.getString("nombre"),
                            rs.getString("numero_documento"),
                            Math.min(100, pct) + "%",
                            rs.getString("evaluado")
                        });
                    }
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error cargando estudiantes.", e);
            alerta("Error al cargar estudiantes: " + e.getMessage());
        } finally { ConexionDB.liberar(con); }
    }

    private void seleccionarEstudiante() {
        int f = tablaEstudiantes.getSelectedRow();
        if (f < 0) return;
        idGrupoEstSeleccionado = (int) modeloEstudiantes.getValueAt(f, 0);
        String evaluado = modeloEstudiantes.getValueAt(f, 4).toString();
        if (evaluado.startsWith("Sí")) {
            alerta("Este estudiante ya tiene evaluación final registrada.\n"
                 + "Una vez registrada no puede modificarse.");
            tablaEstudiantes.clearSelection();
            idGrupoEstSeleccionado = -1;
        }
    }

    private void cargarCriteriosRubrica(JScrollPane scrRub) {
        Object rubItem = cmbRubrica.getSelectedItem();
        if (rubItem == null) return;
        try {
            idRubricaSeleccionada = Integer.parseInt(rubItem.toString().split(" \\| ")[0].trim());
            String minStr = rubItem.toString().replaceAll(".*min: ([0-9.]+).*", "$1");
            lblNotaMinima.setText(minStr);
        } catch (Exception e) { return; }

        criterioFilas.clear();
        String sql =
            "SELECT id, nombre, ponderacion, valor_excelente, valor_bueno, " +
            "valor_aceptable, valor_deficiente FROM rubrica_criterio " +
            "WHERE id_rubrica = ? ORDER BY orden";

        Connection con = null;
        try {
            con = ConexionDB.getConexion();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idRubricaSeleccionada);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        CriterioFila cf = new CriterioFila(
                            rs.getInt("id"), rs.getString("nombre"),
                            rs.getDouble("ponderacion"),
                            rs.getDouble("valor_excelente"), rs.getDouble("valor_bueno"),
                            rs.getDouble("valor_aceptable"), rs.getDouble("valor_deficiente"));
                        cf.cmbNivel.addActionListener(e -> recalcularNota());
                        criterioFilas.add(cf);
                    }
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error cargando criterios.", e);
            alerta("Error cargando criterios de la rúbrica."); return;
        } finally { ConexionDB.liberar(con); }

        // Construir panel de criterios dinámico
        JPanel panCrit = new JPanel(new GridBagLayout());
        panCrit.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 6, 4, 6);

        // Encabezados
        gbc.gridy = 0;
        gbc.gridx = 0; gbc.weightx = 0.4;
        JLabel hCrit = new JLabel("Criterio"); hCrit.setFont(new Font("Segoe UI", Font.BOLD, 11));
        hCrit.setForeground(AZUL); panCrit.add(hCrit, gbc);
        gbc.gridx = 1; gbc.weightx = 0.1;
        JLabel hPond = new JLabel("Pond."); hPond.setFont(new Font("Segoe UI", Font.BOLD, 11));
        hPond.setForeground(AZUL); panCrit.add(hPond, gbc);
        gbc.gridx = 2; gbc.weightx = 0.5;
        JLabel hNiv = new JLabel("Nivel de desempeño"); hNiv.setFont(new Font("Segoe UI", Font.BOLD, 11));
        hNiv.setForeground(AZUL); panCrit.add(hNiv, gbc);

        // Fila por criterio
        for (int i = 0; i < criterioFilas.size(); i++) {
            CriterioFila cf = criterioFilas.get(i);
            gbc.gridy = i + 1;
            gbc.gridx = 0; panCrit.add(cf.lblNombre, gbc);
            gbc.gridx = 1; panCrit.add(cf.lblPonderacion, gbc);
            gbc.gridx = 2; panCrit.add(cf.cmbNivel, gbc);
        }

        scrRub.setViewportView(panCrit);
        panelRubrica.revalidate();
        recalcularNota();
    }

    // ── Cálculo automático de nota ────────────────────────────────────────────

    private void recalcularNota() {
        if (criterioFilas.isEmpty()) return;
        double nota = 0;
        for (CriterioFila cf : criterioFilas) {
            nota += (cf.getValorSeleccionado() * cf.ponderacion) / 100.0;
        }
        nota = Math.round(nota * 100.0) / 100.0;
        lblNotaCalculada.setText(String.valueOf(nota));
        txtNotaDefinitiva.setText(String.valueOf(nota));

        // Colorear según si aprueba
        try {
            double min = Double.parseDouble(lblNotaMinima.getText());
            lblNotaCalculada.setForeground(nota >= min ? VERDE : ROJO);
        } catch (Exception ignored) {}
    }

    // ── Registrar evaluación final ────────────────────────────────────────────

    private void registrarEvaluacion() {
        if (idGrupoEstSeleccionado < 0) {
            alerta("Seleccione un estudiante de la tabla."); return;
        }
        if (idRubricaSeleccionada < 0 || criterioFilas.isEmpty()) {
            alerta("Cargue la rúbrica y sus criterios antes de evaluar."); return;
        }
        String obs = txtObservacionFinal.getText().trim();
        if (obs.isEmpty()) {
            alerta("La observación general es obligatoria."); return;
        }

        double notaCalc;
        double notaDef;
        try {
            notaCalc = Double.parseDouble(lblNotaCalculada.getText());
            notaDef  = Double.parseDouble(txtNotaDefinitiva.getText().trim());
        } catch (NumberFormatException e) {
            alerta("La nota definitiva debe ser un número decimal (ej: 4.5).");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Confirma el registro de la evaluación final?\n" +
            "Nota definitiva: " + notaDef + "\n" +
            "Esta acción no puede revertirse.",
            "Confirmar evaluación final",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        String call = "{call PR_EVAL_FINAL(?,?,?,?,?,?,?)}";
        Connection con = null;
        try {
            con = ConexionDB.getConexion(); con.setAutoCommit(false);
            try (CallableStatement cs = con.prepareCall(call)) {
                cs.setInt(1, idGrupoEstSeleccionado);
                cs.setInt(2, idRubricaSeleccionada);
                cs.setInt(3, docente.getId());
                cs.setDouble(4, notaCalc);
                cs.setDouble(5, notaDef);
                cs.setString(6, obs);
                cs.registerOutParameter(7, Types.VARCHAR);
                cs.execute();
                String resultado = cs.getString(7);
                con.commit();

                if (resultado.startsWith("OK") || resultado.startsWith("ADVERTENCIA")) {
                    JOptionPane.showMessageDialog(this, resultado,
                        "Evaluación registrada", JOptionPane.INFORMATION_MESSAGE);
                    limpiarFormulario();
                    cargarEstudiantesGrupo();
                } else {
                    JOptionPane.showMessageDialog(this, resultado, "Error", JOptionPane.ERROR_MESSAGE);
                    con.rollback();
                }
            }
        } catch (SQLException e) {
            try { if (con!=null) con.rollback(); } catch (SQLException ex) {}
            LOG.log(Level.SEVERE, "Error registrando evaluación: " + e.getMessage(), e);
            alerta("Error: " + e.getMessage());
        } finally {
            try { if (con!=null) con.setAutoCommit(true); } catch (SQLException e) {}
            ConexionDB.liberar(con);
        }
    }

    private void limpiarFormulario() {
        idGrupoEstSeleccionado = -1;
        txtObservacionFinal.setText("");
        txtNotaDefinitiva.setText("0.00");
        lblNotaCalculada.setText("0.00");
        criterioFilas.forEach(cf -> cf.cmbNivel.setSelectedIndex(0));
        tablaEstudiantes.clearSelection();
    }

    // ── Helpers UI ────────────────────────────────────────────────────────────
    private JButton btn(String t, Color c) {
        JButton b = new JButton(t); b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBackground(c); b.setForeground(Color.WHITE);
        b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return b;
    }
    private void alerta(String m) { JOptionPane.showMessageDialog(this, m, "Aviso", JOptionPane.WARNING_MESSAGE); }
}
