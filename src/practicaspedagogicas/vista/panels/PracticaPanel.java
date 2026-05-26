package practicaspedagogicas.vista.panels;

import practicaspedagogicas.dao.ProgramaDAO;
import practicaspedagogicas.modelo.Practica;
import practicaspedagogicas.modelo.Programa;
import practicaspedagogicas.servicio.PracticaServicio;
import practicaspedagogicas.servicio.PracticaServicio.Resultado;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * PracticaPanel - Panel CRUD para la gestión de prácticas pedagógicas.
 * Valida el límite de 8 prácticas por programa y las transiciones de estado.
 *
 * @version 1.0
 */
public class PracticaPanel extends JPanel {

    private final PracticaServicio servicio = new PracticaServicio();
    private final ProgramaDAO      programaDAO = new ProgramaDAO();

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ── Tabla ─────────────────────────────────────────────────────────────────
    private JTable            tabla;
    private DefaultTableModel modeloTabla;

    // ── Filtros ───────────────────────────────────────────────────────────────
    private JComboBox<Programa> cmbFiltroPrograma;

    // ── Formulario ────────────────────────────────────────────────────────────
    private JComboBox<Programa> cmbPrograma;
    private JComboBox<Integer>  cmbNumero;
    private JTextField          txtNombre, txtHoras, txtSemestre, txtFechaInicio, txtFechaFin;
    private JComboBox<String>   cmbTipo, cmbEstado;
    private JTextArea           txtObjetivos;
    private JButton             btnGuardar, btnNuevo, btnEliminar, btnCambiarEstado;

    private Practica practicaSeleccionada = null;

    // ── Colores ───────────────────────────────────────────────────────────────
    private static final Color AZUL   = new Color(0x1F, 0x49, 0x7D);
    private static final Color AZUL_M = new Color(0x2E, 0x75, 0xB6);
    private static final Color VERDE  = new Color(0x27, 0xAE, 0x60);
    private static final Color ROJO   = new Color(0xC0, 0x39, 0x2B);
    private static final Color NARANJA= new Color(0xE6, 0x7E, 0x22);
    private static final Color BG     = new Color(0xF4, 0xF7, 0xFB);

    // ── Constructor ───────────────────────────────────────────────────────────

    public PracticaPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BG);
        setBorder(new EmptyBorder(16, 16, 16, 16));
        initUI();
        cargarProgramasEnCombos();
        cargarDatos();
    }

    // ── UI ────────────────────────────────────────────────────────────────────

    private void initUI() {

        // ── Título + barra de filtro ───────────────────────────────────────────
        JPanel norte = new JPanel(new BorderLayout(10, 6));
        norte.setBackground(BG);

        JLabel lblTitulo = new JLabel("Gestión de Prácticas Pedagógicas");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(AZUL);
        norte.add(lblTitulo, BorderLayout.NORTH);

        JPanel barraFiltro = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        barraFiltro.setBackground(BG);
        barraFiltro.add(new JLabel("Filtrar por programa:"));
        cmbFiltroPrograma = new JComboBox<>();
        cmbFiltroPrograma.setPreferredSize(new Dimension(280, 28));
        barraFiltro.add(cmbFiltroPrograma);
        JButton btnFiltrar  = crearBoton("🔍 Filtrar",  AZUL_M);
        JButton btnTodas    = crearBoton("↺ Todas",    new Color(0x7F, 0x8C, 0x8D));
        barraFiltro.add(btnFiltrar);
        barraFiltro.add(btnTodas);
        norte.add(barraFiltro, BorderLayout.CENTER);
        add(norte, BorderLayout.NORTH);

        // ── Tabla central ─────────────────────────────────────────────────────
        JPanel centro = new JPanel(new BorderLayout(0, 6));
        centro.setBackground(BG);

        String[] cols = {"ID","#","Nombre","Programa","Tipo","Semestre","Horas","Estado"};
        modeloTabla = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = new JTable(modeloTabla);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabla.setRowHeight(26);
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabla.getTableHeader().setBackground(AZUL);
        tabla.getTableHeader().setForeground(Color.WHITE);
        tabla.setSelectionBackground(new Color(0xD6, 0xE4, 0xF0));
        tabla.setGridColor(new Color(0xE0, 0xE0, 0xE0));
        tabla.getColumnModel().getColumn(0).setMaxWidth(45);
        tabla.getColumnModel().getColumn(1).setMaxWidth(35);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xC0, 0xC0, 0xC0)));
        centro.add(scroll, BorderLayout.CENTER);

        // Botones bajo la tabla
        JPanel botonesTabla = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        botonesTabla.setBackground(BG);
        btnNuevo        = crearBoton("+ Nueva práctica", VERDE);
        btnEliminar     = crearBoton("🗑 Desactivar",     ROJO);
        btnCambiarEstado= crearBoton("▶ Cambiar estado",  NARANJA);
        botonesTabla.add(btnNuevo);
        botonesTabla.add(btnEliminar);
        botonesTabla.add(btnCambiarEstado);
        centro.add(botonesTabla, BorderLayout.SOUTH);
        add(centro, BorderLayout.CENTER);

        // ── Formulario derecho ────────────────────────────────────────────────
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xCC, 0xCC, 0xCC)),
            new EmptyBorder(14, 14, 14, 14)
        ));
        form.setPreferredSize(new Dimension(290, 0));

        JLabel lblForm = new JLabel("Datos de la Práctica");
        lblForm.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblForm.setForeground(AZUL);
        lblForm.setAlignmentX(LEFT_ALIGNMENT);
        form.add(lblForm);
        form.add(Box.createVerticalStrut(10));

        // Programa
        form.add(etiqueta("Programa *"));
        cmbPrograma = new JComboBox<>();
        estilizarCombo(cmbPrograma); form.add(cmbPrograma);
        form.add(Box.createVerticalStrut(6));

        // Número de práctica
        form.add(etiqueta("Número (1-8) *"));
        Integer[] nums = {1,2,3,4,5,6,7,8};
        cmbNumero = new JComboBox<>(nums);
        estilizarCombo(cmbNumero); form.add(cmbNumero);
        form.add(Box.createVerticalStrut(6));

        txtNombre     = campo(form, "Nombre de la práctica *");
        txtHoras      = campo(form, "Horas mínimas *");
        txtSemestre   = campo(form, "Semestre (ej: 2026-1)");
        txtFechaInicio= campo(form, "Fecha inicio (dd/MM/yyyy) *");
        txtFechaFin   = campo(form, "Fecha fin (dd/MM/yyyy) *");

        // Tipo
        form.add(etiqueta("Tipo *"));
        cmbTipo = new JComboBox<>(new String[]{
            "Observacion","Intervencion","Investigativa","Profundizacion","Otro"});
        estilizarCombo(cmbTipo); form.add(cmbTipo);
        form.add(Box.createVerticalStrut(6));

        // Estado
        form.add(etiqueta("Estado"));
        cmbEstado = new JComboBox<>(new String[]{
            "Planificada","Activa","Finalizada","Cancelada"});
        estilizarCombo(cmbEstado); form.add(cmbEstado);
        form.add(Box.createVerticalStrut(6));

        // Objetivos
        form.add(etiqueta("Objetivos"));
        txtObjetivos = new JTextArea(3, 18);
        txtObjetivos.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        txtObjetivos.setLineWrap(true); txtObjetivos.setWrapStyleWord(true);
        JScrollPane scrollObj = new JScrollPane(txtObjetivos);
        scrollObj.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        scrollObj.setAlignmentX(LEFT_ALIGNMENT);
        form.add(scrollObj);
        form.add(Box.createVerticalStrut(12));

        btnGuardar = crearBoton("💾 Guardar", AZUL_M);
        btnGuardar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btnGuardar.setAlignmentX(LEFT_ALIGNMENT);
        form.add(btnGuardar);

        add(form, BorderLayout.EAST);

        // ── Eventos ────────────────────────────────────────────────────────────
        tabla.getSelectionModel().addListSelectionListener(
            e -> { if (!e.getValueIsAdjusting()) cargarFormulario(); });

        btnNuevo.addActionListener(e -> limpiarFormulario());
        btnGuardar.addActionListener(e -> guardar());
        btnEliminar.addActionListener(e -> eliminar());
        btnCambiarEstado.addActionListener(e -> cambiarEstado());
        btnFiltrar.addActionListener(e -> filtrarPorPrograma());
        btnTodas.addActionListener(e -> cargarDatos());
    }

    // ── Datos ─────────────────────────────────────────────────────────────────

    private void cargarProgramasEnCombos() {
        List<Programa> programas = programaDAO.listarTodos();

        cmbPrograma.removeAllItems();
        cmbFiltroPrograma.removeAllItems();

        Programa todos = new Programa(); todos.setNombre("-- Todos los programas --"); todos.setId(0);
        cmbFiltroPrograma.addItem(todos);

        for (Programa p : programas) {
            cmbPrograma.addItem(p);
            cmbFiltroPrograma.addItem(p);
        }
    }

    private void cargarDatos() {
        modeloTabla.setRowCount(0);
        servicio.listarTodas().forEach(this::agregarFila);
    }

    private void filtrarPorPrograma() {
        Programa sel = (Programa) cmbFiltroPrograma.getSelectedItem();
        if (sel == null || sel.getId() == 0) { cargarDatos(); return; }
        modeloTabla.setRowCount(0);
        servicio.listarPorPrograma(sel.getId()).forEach(this::agregarFila);
    }

    private void agregarFila(Practica p) {
        modeloTabla.addRow(new Object[]{
            p.getId(), p.getNumero(), p.getNombre(),
            p.getNombrePrograma() != null ? p.getNombrePrograma() : p.getIdPrograma(),
            p.getTipo(), p.getSemestre(), p.getHorasMinimas(), p.getEstado()
        });
    }

    private void cargarFormulario() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) return;
        int id = (int) modeloTabla.getValueAt(fila, 0);
        practicaSeleccionada = servicio.buscarPorId(id);
        if (practicaSeleccionada == null) return;

        // Seleccionar programa en combo
        for (int i = 0; i < cmbPrograma.getItemCount(); i++) {
            if (cmbPrograma.getItemAt(i).getId() == practicaSeleccionada.getIdPrograma()) {
                cmbPrograma.setSelectedIndex(i); break;
            }
        }
        cmbNumero.setSelectedItem(practicaSeleccionada.getNumero());
        txtNombre.setText(practicaSeleccionada.getNombre());
        txtHoras.setText(String.valueOf(practicaSeleccionada.getHorasMinimas()));
        txtSemestre.setText(practicaSeleccionada.getSemestre() != null ? practicaSeleccionada.getSemestre() : "");
        txtFechaInicio.setText(practicaSeleccionada.getFechaInicio() != null
            ? practicaSeleccionada.getFechaInicio().format(FMT) : "");
        txtFechaFin.setText(practicaSeleccionada.getFechaFin() != null
            ? practicaSeleccionada.getFechaFin().format(FMT) : "");
        cmbTipo.setSelectedItem(practicaSeleccionada.getTipo());
        cmbEstado.setSelectedItem(practicaSeleccionada.getEstado());
        txtObjetivos.setText(practicaSeleccionada.getObjetivos() != null ? practicaSeleccionada.getObjetivos() : "");
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    private void guardar() {
        Programa prog = (Programa) cmbPrograma.getSelectedItem();
        String nombre = txtNombre.getText().trim();
        String horasStr = txtHoras.getText().trim();

        if (prog == null || nombre.isEmpty() || horasStr.isEmpty()
                || txtFechaInicio.getText().isEmpty() || txtFechaFin.getText().isEmpty()) {
            alerta("Complete los campos obligatorios (*).");
            return;
        }

        int horas;
        try { horas = Integer.parseInt(horasStr); }
        catch (NumberFormatException e) { alerta("Las horas deben ser un número entero."); return; }

        LocalDate fechaInicio, fechaFin;
        try {
            fechaInicio = LocalDate.parse(txtFechaInicio.getText().trim(), FMT);
            fechaFin    = LocalDate.parse(txtFechaFin.getText().trim(), FMT);
        } catch (DateTimeParseException e) {
            alerta("Formato de fecha incorrecto. Use dd/MM/yyyy."); return;
        }

        if (practicaSeleccionada == null) {
            // INSERT
            Practica pr = new Practica(
                prog.getId(),
                (Integer) cmbNumero.getSelectedItem(),
                nombre,
                (String) cmbTipo.getSelectedItem(),
                txtObjetivos.getText().trim(),
                horas,
                txtSemestre.getText().trim(),
                fechaInicio, fechaFin
            );
            Resultado<Practica> res = servicio.crear(pr);
            mostrarResultado(res);
        } else {
            // UPDATE
            practicaSeleccionada.setIdPrograma(prog.getId());
            practicaSeleccionada.setNumero((Integer) cmbNumero.getSelectedItem());
            practicaSeleccionada.setNombre(nombre);
            practicaSeleccionada.setTipo((String) cmbTipo.getSelectedItem());
            practicaSeleccionada.setObjetivos(txtObjetivos.getText().trim());
            practicaSeleccionada.setHorasMinimas(horas);
            practicaSeleccionada.setSemestre(txtSemestre.getText().trim());
            practicaSeleccionada.setFechaInicio(fechaInicio);
            practicaSeleccionada.setFechaFin(fechaFin);
            practicaSeleccionada.setEstado((String) cmbEstado.getSelectedItem());
            Resultado<Practica> res = servicio.actualizar(practicaSeleccionada);
            mostrarResultado(res);
        }

        limpiarFormulario();
        cargarDatos();
    }

    private void eliminar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) { alerta("Seleccione una práctica."); return; }
        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Desactivar la práctica seleccionada?", "Confirmar",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            int id = (int) modeloTabla.getValueAt(fila, 0);
            Resultado<Void> res = servicio.eliminar(id);
            mostrarResultado(res);
            limpiarFormulario();
            cargarDatos();
        }
    }

    private void cambiarEstado() {
        if (practicaSeleccionada == null) { alerta("Seleccione una práctica."); return; }
        String[] opciones = {"Activa", "Finalizada", "Cancelada"};
        String nuevo = (String) JOptionPane.showInputDialog(this,
            "Seleccione el nuevo estado:", "Cambiar Estado",
            JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);
        if (nuevo != null) {
            Resultado<Void> res = servicio.cambiarEstado(practicaSeleccionada.getId(), nuevo);
            mostrarResultado(res);
            cargarDatos();
        }
    }

    private void limpiarFormulario() {
        practicaSeleccionada = null;
        if (cmbPrograma.getItemCount() > 0) cmbPrograma.setSelectedIndex(0);
        cmbNumero.setSelectedIndex(0);
        txtNombre.setText(""); txtHoras.setText(""); txtSemestre.setText("");
        txtFechaInicio.setText(""); txtFechaFin.setText("");
        cmbTipo.setSelectedIndex(0); cmbEstado.setSelectedIndex(0);
        txtObjetivos.setText("");
        tabla.clearSelection();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private JLabel etiqueta(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(AZUL);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }

    private JTextField campo(JPanel panel, String etiqueta) {
        panel.add(etiqueta(etiqueta));
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        tf.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(tf);
        panel.add(Box.createVerticalStrut(6));
        return tf;
    }

    private void estilizarCombo(JComboBox<?> cmb) {
        cmb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        cmb.setAlignmentX(LEFT_ALIGNMENT);
    }

    private JButton crearBoton(String texto, Color color) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(color); btn.setForeground(Color.WHITE);
        btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void alerta(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Aviso", JOptionPane.WARNING_MESSAGE);
    }

    private <T> void mostrarResultado(Resultado<T> res) {
        if (res.isExito())
            JOptionPane.showMessageDialog(this, res.getMensaje(), "Éxito",
                JOptionPane.INFORMATION_MESSAGE);
        else
            JOptionPane.showMessageDialog(this, res.getMensaje(), "Error",
                JOptionPane.ERROR_MESSAGE);
    }
}
