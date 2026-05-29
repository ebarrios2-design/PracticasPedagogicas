package practicaspedagogicas.vista.panels;

import practicaspedagogicas.dao.ProgramaDAO;
import practicaspedagogicas.dao.UsuarioDAO;
import practicaspedagogicas.modelo.Programa;
import practicaspedagogicas.modelo.Usuario;
import practicaspedagogicas.util.HashUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

/**
 * UsuarioPanel - Panel CRUD para la gestión de usuarios del sistema.
 * Permite crear, editar, activar/desactivar usuarios y asignar roles.
 *
 * @version 1.0
 */
public class UsuarioPanel extends JPanel {

    private final UsuarioDAO  usuarioDAO  = new UsuarioDAO();
    private final ProgramaDAO programaDAO = new ProgramaDAO();

    // ── Tabla ─────────────────────────────────────────────────────────────────
    private JTable            tabla;
    private DefaultTableModel modeloTabla;

    // ── Filtros ───────────────────────────────────────────────────────────────
    private JComboBox<String> cmbFiltroRol;
    private JTextField        txtBuscar;

    // ── Formulario ────────────────────────────────────────────────────────────
    private JTextField        txtNombres, txtApellidos, txtDocumento,
                              txtCorreo, txtTelefono;
    private JComboBox<String> cmbTipoDoc, cmbRol;
    private JComboBox<Programa> cmbPrograma;
    private JPasswordField    txtContrasena;
    private JButton           btnGuardar, btnNuevo, btnDesactivar, btnResetPass;

    private Usuario usuarioSeleccionado = null;

    // ── Colores ───────────────────────────────────────────────────────────────
    private static final Color AZUL   = new Color(0x1F, 0x49, 0x7D);
    private static final Color AZUL_M = new Color(0x2E, 0x75, 0xB6);
    private static final Color VERDE  = new Color(0x27, 0xAE, 0x60);
    private static final Color ROJO   = new Color(0xC0, 0x39, 0x2B);
    private static final Color MORADO = new Color(0x8E, 0x44, 0xAD);
    private static final Color BG     = new Color(0xF4, 0xF7, 0xFB);

    // ── Constructor ───────────────────────────────────────────────────────────

    public UsuarioPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BG);
        setBorder(new EmptyBorder(16, 16, 16, 16));
        initUI();
        cargarProgramas();
        cargarDatos();
    }

    // ── UI ────────────────────────────────────────────────────────────────────

    private void initUI() {

        // ── Título ─────────────────────────────────────────────────────────────
        JPanel norte = new JPanel(new BorderLayout(10, 6));
        norte.setBackground(BG);
        JLabel lblTitulo = new JLabel("Gestión de Usuarios del Sistema");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(AZUL);
        norte.add(lblTitulo, BorderLayout.NORTH);

        // Barra de filtros
        JPanel barraFiltro = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        barraFiltro.setBackground(BG);
        barraFiltro.add(new JLabel("Rol:"));
        cmbFiltroRol = new JComboBox<>(new String[]{
            "Todos","Director","Coordinador","Docente","Estudiante","Institucion"});
        cmbFiltroRol.setPreferredSize(new Dimension(140, 28));
        barraFiltro.add(cmbFiltroRol);
        barraFiltro.add(new JLabel("  Buscar:"));
        txtBuscar = new JTextField(16);
        barraFiltro.add(txtBuscar);
        JButton btnFiltrar = crearBoton("🔍 Filtrar", AZUL_M);
        JButton btnTodos   = crearBoton("↺ Todos",   new Color(0x7F, 0x8C, 0x8D));
        barraFiltro.add(btnFiltrar);
        barraFiltro.add(btnTodos);
        norte.add(barraFiltro, BorderLayout.CENTER);
        add(norte, BorderLayout.NORTH);

        // ── Tabla ─────────────────────────────────────────────────────────────
        JPanel centro = new JPanel(new BorderLayout(0, 6));
        centro.setBackground(BG);

        String[] cols = {"ID","Nombres","Apellidos","Documento","Correo","Rol","Activo"};
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

        // Coloreado de filas según rol
        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel) {
                    String rol = (String) modeloTabla.getValueAt(row, 5);
                    switch (rol != null ? rol : "") {
                        case "Director":    c.setBackground(new Color(0xFF, 0xF0, 0xF0)); break;
                        case "Coordinador": c.setBackground(new Color(0xFF, 0xF9, 0xE6)); break;
                        case "Docente":     c.setBackground(new Color(0xEA, 0xF4, 0xFF)); break;
                        case "Estudiante":  c.setBackground(new Color(0xF0, 0xFF, 0xF0)); break;
                        default:            c.setBackground(Color.WHITE);
                    }
                }
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xC0, 0xC0, 0xC0)));
        centro.add(scroll, BorderLayout.CENTER);

        // Botones bajo la tabla
        JPanel botonesTabla = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        botonesTabla.setBackground(BG);
        btnNuevo     = crearBoton("+ Nuevo usuario", VERDE);
        btnDesactivar= crearBoton("🗑 Desactivar",    ROJO);
        btnResetPass = crearBoton("🔑 Reset contraseña", MORADO);
        botonesTabla.add(btnNuevo);
        botonesTabla.add(btnDesactivar);
        botonesTabla.add(btnResetPass);
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

        JLabel lblForm = new JLabel("Datos del Usuario");
        lblForm.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblForm.setForeground(AZUL); lblForm.setAlignmentX(LEFT_ALIGNMENT);
        form.add(lblForm);
        form.add(Box.createVerticalStrut(10));

        txtNombres   = campo(form, "Nombres *");
        txtApellidos = campo(form, "Apellidos *");

        form.add(etiqueta("Tipo de documento *"));
        cmbTipoDoc = new JComboBox<>(new String[]{"CC","CE","TI","Pasaporte"});
        estilizarCombo(cmbTipoDoc); form.add(cmbTipoDoc);
        form.add(Box.createVerticalStrut(6));

        txtDocumento = campo(form, "Número de documento *");
        txtCorreo    = campo(form, "Correo institucional *");
        txtTelefono  = campo(form, "Teléfono");

        form.add(etiqueta("Rol *"));
        cmbRol = new JComboBox<>(new String[]{
            "Director","Coordinador","Docente","Estudiante","Institucion"});
        estilizarCombo(cmbRol); form.add(cmbRol);
        form.add(Box.createVerticalStrut(6));

        form.add(etiqueta("Programa"));
        cmbPrograma = new JComboBox<>();
        estilizarCombo(cmbPrograma); form.add(cmbPrograma);
        form.add(Box.createVerticalStrut(6));

        form.add(etiqueta("Contraseña (solo para nuevos usuarios)"));
        txtContrasena = new JPasswordField();
        txtContrasena.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtContrasena.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        txtContrasena.setAlignmentX(LEFT_ALIGNMENT);
        form.add(txtContrasena);
        form.add(Box.createVerticalStrut(14));

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
        btnDesactivar.addActionListener(e -> desactivar());
        btnResetPass.addActionListener(e -> resetContrasena());
        btnFiltrar.addActionListener(e -> filtrar());
        btnTodos.addActionListener(e -> { cmbFiltroRol.setSelectedIndex(0); txtBuscar.setText(""); cargarDatos(); });
    }

    // ── Datos ─────────────────────────────────────────────────────────────────

    private void cargarProgramas() {
        cmbPrograma.removeAllItems();
        Programa ninguno = new Programa();
        ninguno.setId(0); ninguno.setNombre("-- Sin programa --");
        cmbPrograma.addItem(ninguno);
        programaDAO.listarTodos().forEach(cmbPrograma::addItem);
    }

    private void cargarDatos() {
        modeloTabla.setRowCount(0);
        usuarioDAO.listarTodos().forEach(this::agregarFila);
    }

    private void filtrar() {
        String rol    = (String) cmbFiltroRol.getSelectedItem();
        String buscar = txtBuscar.getText().trim().toLowerCase();
        modeloTabla.setRowCount(0);

        List<Usuario> lista = "Todos".equals(rol)
            ? usuarioDAO.listarTodos()
            : usuarioDAO.listarPorRol(rol);

        lista.stream()
            .filter(u -> buscar.isEmpty()
                || u.getNombreCompleto().toLowerCase().contains(buscar)
                || u.getCorreo().toLowerCase().contains(buscar)
                || u.getNumeroDocumento().contains(buscar))
            .forEach(this::agregarFila);
    }

    private void agregarFila(Usuario u) {
        modeloTabla.addRow(new Object[]{
            u.getId(), u.getNombres(), u.getApellidos(),
            u.getNumeroDocumento(), u.getCorreo(),
            u.getRol(), u.isActivo() ? "Sí" : "No"
        });
    }

    private void cargarFormulario() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) return;
        int id = (int) modeloTabla.getValueAt(fila, 0);
        usuarioSeleccionado = usuarioDAO.buscarPorId(id);
        if (usuarioSeleccionado == null) return;

        txtNombres.setText(usuarioSeleccionado.getNombres());
        txtApellidos.setText(usuarioSeleccionado.getApellidos());
        cmbTipoDoc.setSelectedItem(usuarioSeleccionado.getTipoDocumento());
        txtDocumento.setText(usuarioSeleccionado.getNumeroDocumento());
        txtCorreo.setText(usuarioSeleccionado.getCorreo());
        txtTelefono.setText(usuarioSeleccionado.getTelefono() != null ? usuarioSeleccionado.getTelefono() : "");
        cmbRol.setSelectedItem(usuarioSeleccionado.getRol());
        txtContrasena.setText("");  // no mostrar hash

        // Seleccionar programa
        if (usuarioSeleccionado.getIdPrograma() != null) {
            for (int i = 0; i < cmbPrograma.getItemCount(); i++) {
                if (cmbPrograma.getItemAt(i).getId() == usuarioSeleccionado.getIdPrograma()) {
                    cmbPrograma.setSelectedIndex(i); break;
                }
            }
        } else {
            cmbPrograma.setSelectedIndex(0);
        }
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    private void guardar() {
        String nombres   = txtNombres.getText().trim();
        String apellidos = txtApellidos.getText().trim();
        String documento = txtDocumento.getText().trim();
        String correo    = txtCorreo.getText().trim();

        if (nombres.isEmpty() || apellidos.isEmpty() || documento.isEmpty() || correo.isEmpty()) {
            alerta("Complete los campos obligatorios (*).");
            return;
        }

        Programa progSel = (Programa) cmbPrograma.getSelectedItem();
        Integer idProg   = (progSel != null && progSel.getId() != 0) ? progSel.getId() : null;

        if (usuarioSeleccionado == null) {
            // INSERT
            String pass = new String(txtContrasena.getPassword()).trim();
            if (pass.length() < 6) {
                alerta("La contraseña debe tener al menos 6 caracteres.");
                return;
            }
            String hash = HashUtil.sha256(pass);
            Usuario u = new Usuario(idProg, nombres, apellidos,
                (String) cmbTipoDoc.getSelectedItem(), documento, correo, hash,
                (String) cmbRol.getSelectedItem());
            u.setTelefono(txtTelefono.getText().trim());

            boolean ok = usuarioDAO.insertar(u);
            if (ok) exito("Usuario creado exitosamente.");
            else    error("No se pudo crear el usuario. Verifique que el correo y documento no estén duplicados.");
        } else {
            // UPDATE
            usuarioSeleccionado.setNombres(nombres);
            usuarioSeleccionado.setApellidos(apellidos);
            usuarioSeleccionado.setTipoDocumento((String) cmbTipoDoc.getSelectedItem());
            usuarioSeleccionado.setNumeroDocumento(documento);
            usuarioSeleccionado.setCorreo(correo);
            usuarioSeleccionado.setRol((String) cmbRol.getSelectedItem());
            usuarioSeleccionado.setTelefono(txtTelefono.getText().trim());
            usuarioSeleccionado.setIdPrograma(idProg);

            boolean ok = usuarioDAO.actualizar(usuarioSeleccionado);
            if (ok) exito("Usuario actualizado correctamente.");
            else    error("No se pudo actualizar el usuario.");
        }

        limpiarFormulario();
        cargarDatos();
    }

    private void desactivar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) { alerta("Seleccione un usuario."); return; }
        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Desactivar al usuario seleccionado?\nEl usuario no podrá iniciar sesión.",
            "Confirmar desactivación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            int id = (int) modeloTabla.getValueAt(fila, 0);
            boolean ok = usuarioDAO.eliminar(id);
            if (ok) exito("Usuario desactivado.");
            else    error("No se pudo desactivar el usuario.");
            limpiarFormulario();
            cargarDatos();
        }
    }

    private void resetContrasena() {
        if (usuarioSeleccionado == null) { alerta("Seleccione un usuario."); return; }
        String nueva = JOptionPane.showInputDialog(this,
            "Ingrese la nueva contraseña (mínimo 6 caracteres):",
            "Restablecer contraseña", JOptionPane.QUESTION_MESSAGE);
        if (nueva == null || nueva.trim().length() < 6) {
            alerta("La contraseña debe tener al menos 6 caracteres.");
            return;
        }
        String hash = HashUtil.sha256(nueva.trim());
        boolean ok  = usuarioDAO.cambiarContrasena(usuarioSeleccionado.getId(), hash);
        if (ok) exito("Contraseña restablecida exitosamente.");
        else    error("No se pudo restablecer la contraseña.");
    }

    private void limpiarFormulario() {
        usuarioSeleccionado = null;
        txtNombres.setText(""); txtApellidos.setText("");
        txtDocumento.setText(""); txtCorreo.setText(""); txtTelefono.setText("");
        cmbTipoDoc.setSelectedIndex(0);
        cmbRol.setSelectedIndex(0);
        cmbPrograma.setSelectedIndex(0);
        txtContrasena.setText("");
        tabla.clearSelection();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private JLabel etiqueta(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(AZUL); l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JTextField campo(JPanel p, String etiqueta) {
        p.add(etiqueta(etiqueta));
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        tf.setAlignmentX(LEFT_ALIGNMENT);
        p.add(tf); p.add(Box.createVerticalStrut(6));
        return tf;
    }

    private void estilizarCombo(JComboBox<?> c) {
        c.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        c.setAlignmentX(LEFT_ALIGNMENT);
    }

    private JButton crearBoton(String t, Color c) {
        JButton b = new JButton(t);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBackground(c); b.setForeground(Color.WHITE);
        b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void alerta(String m) { JOptionPane.showMessageDialog(this, m, "Aviso",  JOptionPane.WARNING_MESSAGE); }
    private void exito (String m) { JOptionPane.showMessageDialog(this, m, "Éxito",  JOptionPane.INFORMATION_MESSAGE); }
    private void error (String m) { JOptionPane.showMessageDialog(this, m, "Error",  JOptionPane.ERROR_MESSAGE); }
}
