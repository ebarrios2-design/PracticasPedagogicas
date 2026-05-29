package practicaspedagogicas.vista.panels;

import practicaspedagogicas.dao.ProgramaDAO;
import practicaspedagogicas.modelo.Programa;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * ProgramaPanel - Panel CRUD para la gestión de programas de licenciatura.
 * Permite listar, crear, editar y desactivar programas.
 *
 * @version 1.0
 */
public class ProgramaPanel extends JPanel {

    // ── Datos ─────────────────────────────────────────────────────────────────
    private final ProgramaDAO dao = new ProgramaDAO();

    // ── Componentes tabla ─────────────────────────────────────────────────────
    private JTable             tabla;
    private DefaultTableModel  modeloTabla;
    private JTextField         txtBuscar;

    // ── Formulario ────────────────────────────────────────────────────────────
    private JTextField  txtNombre, txtSnies, txtFacultad, txtSemestre;
    private JComboBox<String> cmbModalidad, cmbNivel;
    private JCheckBox   chkAcreditado;
    private JButton     btnGuardar, btnNuevo, btnEliminar;

    private Programa programaSeleccionado = null;

    // ── Colores ───────────────────────────────────────────────────────────────
    private static final Color AZUL    = new Color(0x1F, 0x49, 0x7D);
    private static final Color AZUL_M  = new Color(0x2E, 0x75, 0xB6);
    private static final Color VERDE   = new Color(0x27, 0xAE, 0x60);
    private static final Color ROJO    = new Color(0xC0, 0x39, 0x2B);
    private static final Color BG      = new Color(0xF4, 0xF7, 0xFB);

    // ── Constructor ───────────────────────────────────────────────────────────

    public ProgramaPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BG);
        setBorder(new EmptyBorder(16, 16, 16, 16));
        initUI();
        cargarDatos();
    }

    // ── Construcción UI ───────────────────────────────────────────────────────

    private void initUI() {
        // ── Título ─────────────────────────────────────────────────────────────
        JLabel lblTitulo = new JLabel("Gestión de Programas de Licenciatura");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(AZUL);
        lblTitulo.setBorder(new EmptyBorder(0, 0, 10, 0));
        add(lblTitulo, BorderLayout.NORTH);

        // ── Panel izquierdo: tabla + búsqueda ──────────────────────────────────
        JPanel izquierdo = new JPanel(new BorderLayout(5, 5));
        izquierdo.setBackground(BG);

        // Barra de búsqueda
        JPanel barraFiltro = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        barraFiltro.setBackground(BG);
        txtBuscar = new JTextField(20);
        txtBuscar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JButton btnBuscar = crearBoton("🔍 Buscar", AZUL_M);
        JButton btnRefresh= crearBoton("↺ Todos",  new Color(0x7F, 0x8C, 0x8D));
        barraFiltro.add(new JLabel("Buscar: "));
        barraFiltro.add(txtBuscar);
        barraFiltro.add(btnBuscar);
        barraFiltro.add(btnRefresh);
        izquierdo.add(barraFiltro, BorderLayout.NORTH);

        // Tabla
        String[] columnas = {"ID", "Nombre", "SNIES", "Facultad", "Modalidad", "Nivel", "Acreditado"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
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

        // Ajustar ancho columna ID
        tabla.getColumnModel().getColumn(0).setMaxWidth(45);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xC0, 0xC0, 0xC0)));
        izquierdo.add(scroll, BorderLayout.CENTER);

        // Botones de acción bajo la tabla
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        botones.setBackground(BG);
        btnNuevo   = crearBoton("+ Nuevo",   VERDE);
        btnEliminar= crearBoton("🗑 Desactivar", ROJO);
        botones.add(btnNuevo);
        botones.add(btnEliminar);
        izquierdo.add(botones, BorderLayout.SOUTH);

        add(izquierdo, BorderLayout.CENTER);

        // ── Panel derecho: formulario ──────────────────────────────────────────
        JPanel formulario = new JPanel();
        formulario.setLayout(new BoxLayout(formulario, BoxLayout.Y_AXIS));
        formulario.setBackground(Color.WHITE);
        formulario.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xCC, 0xCC, 0xCC)),
            new EmptyBorder(16, 16, 16, 16)
        ));
        formulario.setPreferredSize(new Dimension(280, 0));

        JLabel lblForm = new JLabel("Datos del Programa");
        lblForm.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblForm.setForeground(AZUL);
        lblForm.setAlignmentX(LEFT_ALIGNMENT);
        formulario.add(lblForm);
        formulario.add(Box.createVerticalStrut(12));

        txtNombre   = agregarCampoForm(formulario, "Nombre *");
        txtSnies    = agregarCampoForm(formulario, "Código SNIES");
        txtFacultad = agregarCampoForm(formulario, "Facultad *");

        cmbModalidad = agregarComboForm(formulario, "Modalidad *",
            new String[]{"Presencial","Virtual","A distancia","Mixta"});
        cmbNivel = agregarComboForm(formulario, "Nivel",
            new String[]{"Pregrado","Posgrado"});

        chkAcreditado = new JCheckBox("Programa Acreditado");
        chkAcreditado.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkAcreditado.setBackground(Color.WHITE);
        chkAcreditado.setAlignmentX(LEFT_ALIGNMENT);
        formulario.add(chkAcreditado);
        formulario.add(Box.createVerticalStrut(16));

        btnGuardar = crearBoton("💾 Guardar", AZUL_M);
        btnGuardar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btnGuardar.setAlignmentX(LEFT_ALIGNMENT);
        formulario.add(btnGuardar);

        add(formulario, BorderLayout.EAST);

        // ── Eventos ────────────────────────────────────────────────────────────
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) cargarFormulario();
        });

        btnNuevo.addActionListener(e -> limpiarFormulario());
        btnGuardar.addActionListener(e -> guardar());
        btnEliminar.addActionListener(e -> eliminar());
        btnBuscar.addActionListener(e -> buscar());
        btnRefresh.addActionListener(e -> { txtBuscar.setText(""); cargarDatos(); });
    }

    // ── Operaciones CRUD ──────────────────────────────────────────────────────

    private void cargarDatos() {
        modeloTabla.setRowCount(0);
        List<Programa> lista = dao.listarTodos();
        for (Programa p : lista) {
            modeloTabla.addRow(new Object[]{
                p.getId(), p.getNombre(), p.getCodigoSnies(),
                p.getFacultad(), p.getModalidad(), p.getNivel(),
                p.isAcreditado() ? "Sí" : "No"
            });
        }
    }

    private void buscar() {
        String texto = txtBuscar.getText().trim();
        if (texto.isEmpty()) { cargarDatos(); return; }
        modeloTabla.setRowCount(0);
        dao.buscarPorNombre(texto).forEach(p ->
            modeloTabla.addRow(new Object[]{
                p.getId(), p.getNombre(), p.getCodigoSnies(),
                p.getFacultad(), p.getModalidad(), p.getNivel(),
                p.isAcreditado() ? "Sí" : "No"
            })
        );
    }

    private void cargarFormulario() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) return;
        int id = (int) modeloTabla.getValueAt(fila, 0);
        programaSeleccionado = dao.buscarPorId(id);
        if (programaSeleccionado == null) return;

        txtNombre.setText(programaSeleccionado.getNombre());
        txtSnies.setText(programaSeleccionado.getCodigoSnies());
        txtFacultad.setText(programaSeleccionado.getFacultad());
        cmbModalidad.setSelectedItem(programaSeleccionado.getModalidad());
        cmbNivel.setSelectedItem(programaSeleccionado.getNivel());
        chkAcreditado.setSelected(programaSeleccionado.isAcreditado());
    }

    private void guardar() {
        String nombre   = txtNombre.getText().trim();
        String facultad = txtFacultad.getText().trim();

        if (nombre.isEmpty() || facultad.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "El nombre y la facultad son obligatorios.", "Validación",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (programaSeleccionado == null) {
            // INSERTAR
            Programa p = new Programa(nombre, txtSnies.getText().trim(),
                facultad, (String) cmbModalidad.getSelectedItem(),
                (String) cmbNivel.getSelectedItem(), chkAcreditado.isSelected());
            boolean ok = dao.insertar(p);
            mostrarResultado(ok, "Programa guardado.", "No se pudo guardar.");
        } else {
            // ACTUALIZAR
            programaSeleccionado.setNombre(nombre);
            programaSeleccionado.setCodigoSnies(txtSnies.getText().trim());
            programaSeleccionado.setFacultad(facultad);
            programaSeleccionado.setModalidad((String) cmbModalidad.getSelectedItem());
            programaSeleccionado.setNivel((String) cmbNivel.getSelectedItem());
            programaSeleccionado.setAcreditado(chkAcreditado.isSelected());
            boolean ok = dao.actualizar(programaSeleccionado);
            mostrarResultado(ok, "Programa actualizado.", "No se pudo actualizar.");
        }

        limpiarFormulario();
        cargarDatos();
    }

    private void eliminar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un programa de la tabla.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Desactivar el programa seleccionado?", "Confirmar",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            int id = (int) modeloTabla.getValueAt(fila, 0);
            boolean ok = dao.eliminar(id);
            mostrarResultado(ok, "Programa desactivado.", "No se pudo desactivar.");
            limpiarFormulario();
            cargarDatos();
        }
    }

    // ── Helpers de UI ─────────────────────────────────────────────────────────

    private void limpiarFormulario() {
        programaSeleccionado = null;
        txtNombre.setText(""); txtSnies.setText(""); txtFacultad.setText("");
        cmbModalidad.setSelectedIndex(0); cmbNivel.setSelectedIndex(0);
        chkAcreditado.setSelected(false);
        tabla.clearSelection();
    }

    private JTextField agregarCampoForm(JPanel panel, String etiqueta) {
        JLabel lbl = new JLabel(etiqueta);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(AZUL);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(lbl);

        JTextField campo = new JTextField();
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        campo.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(campo);
        panel.add(Box.createVerticalStrut(8));
        return campo;
    }

    private JComboBox<String> agregarComboForm(JPanel panel, String etiqueta, String[] opciones) {
        JLabel lbl = new JLabel(etiqueta);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(AZUL);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(lbl);

        JComboBox<String> combo = new JComboBox<>(opciones);
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        combo.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(combo);
        panel.add(Box.createVerticalStrut(8));
        return combo;
    }

    private JButton crearBoton(String texto, Color color) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void mostrarResultado(boolean ok, String msgOk, String msgError) {
        if (ok) {
            JOptionPane.showMessageDialog(this, msgOk, "Éxito",
                JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, msgError, "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
