package practicaspedagogicas.vista.panels;

import practicaspedagogicas.dao.InstitucionReceptoraDAO;
import practicaspedagogicas.modelo.InstitucionReceptora;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * InstitucionPanel - CRUD de instituciones educativas receptoras.
 * Gestiona el registro de colegios y centros donde se realizan las prácticas.
 *
 * @version 1.0 - Oracle 10g XE
 */
public class InstitucionPanel extends JPanel {

    private final InstitucionReceptoraDAO dao = new InstitucionReceptoraDAO();

    // ── Tabla ─────────────────────────────────────────────────────────────────
    private JTable            tabla;
    private DefaultTableModel modeloTabla;
    private JTextField        txtBuscar;

    // ── Formulario ────────────────────────────────────────────────────────────
    private JTextField  txtNombre, txtNit, txtDane, txtDireccion,
                        txtMunicipio, txtDepartamento, txtRector,
                        txtCorreo, txtTelefono;
    private JComboBox<String> cmbZona;
    private JCheckBox   chkPreescolar, chkPrimaria, chkSecundaria, chkMedia;
    private JButton     btnGuardar, btnNuevo, btnEliminar;

    private InstitucionReceptora seleccionada = null;

    // ── Colores ───────────────────────────────────────────────────────────────
    private static final Color AZUL   = new Color(0x1F, 0x49, 0x7D);
    private static final Color AZUL_M = new Color(0x2E, 0x75, 0xB6);
    private static final Color VERDE  = new Color(0x27, 0xAE, 0x60);
    private static final Color ROJO   = new Color(0xC0, 0x39, 0x2B);
    private static final Color BG     = new Color(0xF4, 0xF7, 0xFB);

    public InstitucionPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BG);
        setBorder(new EmptyBorder(16, 16, 16, 16));
        initUI();
        cargarDatos();
    }

    private void initUI() {
        // Título
        JLabel lbl = new JLabel("Gestión de Instituciones Receptoras");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lbl.setForeground(AZUL);
        lbl.setBorder(new EmptyBorder(0, 0, 10, 0));
        add(lbl, BorderLayout.NORTH);

        // ── Panel central: buscador + tabla ───────────────────────────────────
        JPanel centro = new JPanel(new BorderLayout(5, 5));
        centro.setBackground(BG);

        JPanel barraFiltro = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        barraFiltro.setBackground(BG);
        txtBuscar = new JTextField(20);
        txtBuscar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JButton btnBuscar  = btn("🔍 Buscar", AZUL_M);
        JButton btnRefresh = btn("↺ Todos",  new Color(0x7F, 0x8C, 0x8D));
        barraFiltro.add(new JLabel("Municipio:"));
        barraFiltro.add(txtBuscar);
        barraFiltro.add(btnBuscar);
        barraFiltro.add(btnRefresh);
        centro.add(barraFiltro, BorderLayout.NORTH);

        String[] cols = {"ID","Nombre","NIT","Municipio","Departamento","Zona","Activo"};
        modeloTabla = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = new JTable(modeloTabla);
        estilizarTabla(tabla);
        tabla.getColumnModel().getColumn(0).setMaxWidth(45);
        centro.add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        botones.setBackground(BG);
        btnNuevo   = btn("+ Nueva institución", VERDE);
        btnEliminar= btn("🗑 Desactivar",        ROJO);
        botones.add(btnNuevo); botones.add(btnEliminar);
        centro.add(botones, BorderLayout.SOUTH);
        add(centro, BorderLayout.CENTER);

        // ── Formulario derecho ────────────────────────────────────────────────
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xCC, 0xCC, 0xCC)),
            new EmptyBorder(14, 14, 14, 14)));
        form.setPreferredSize(new Dimension(290, 0));

        JLabel lblF = new JLabel("Datos de la Institución");
        lblF.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblF.setForeground(AZUL); lblF.setAlignmentX(LEFT_ALIGNMENT);
        form.add(lblF);
        form.add(Box.createVerticalStrut(10));

        txtNombre      = campo(form, "Nombre *");
        txtNit         = campo(form, "NIT");
        txtDane        = campo(form, "Código DANE");
        txtDireccion   = campo(form, "Dirección *");
        txtMunicipio   = campo(form, "Municipio *");
        txtDepartamento= campo(form, "Departamento *");

        form.add(etq("Zona *"));
        cmbZona = new JComboBox<>(new String[]{"Urbana","Rural"});
        estCmb(cmbZona); form.add(cmbZona);
        form.add(Box.createVerticalStrut(6));

        // Niveles educativos (checkboxes)
        form.add(etq("Niveles educativos *"));
        JPanel pnlNiveles = new JPanel(new GridLayout(2, 2, 4, 2));
        pnlNiveles.setBackground(Color.WHITE);
        pnlNiveles.setAlignmentX(LEFT_ALIGNMENT);
        pnlNiveles.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        chkPreescolar = chk("Preescolar"); chkPrimaria  = chk("Primaria");
        chkSecundaria = chk("Secundaria"); chkMedia      = chk("Media");
        pnlNiveles.add(chkPreescolar); pnlNiveles.add(chkPrimaria);
        pnlNiveles.add(chkSecundaria); pnlNiveles.add(chkMedia);
        form.add(pnlNiveles);
        form.add(Box.createVerticalStrut(6));

        txtRector  = campo(form, "Rector / Directivo");
        txtCorreo  = campo(form, "Correo de contacto");
        txtTelefono= campo(form, "Teléfono");

        btnGuardar = btn("💾 Guardar", AZUL_M);
        btnGuardar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btnGuardar.setAlignmentX(LEFT_ALIGNMENT);
        form.add(Box.createVerticalStrut(10));
        form.add(btnGuardar);
        add(form, BorderLayout.EAST);

        // ── Eventos ────────────────────────────────────────────────────────────
        tabla.getSelectionModel().addListSelectionListener(
            e -> { if (!e.getValueIsAdjusting()) cargarFormulario(); });
        btnNuevo.addActionListener(e -> limpiar());
        btnGuardar.addActionListener(e -> guardar());
        btnEliminar.addActionListener(e -> eliminar());
        btnBuscar.addActionListener(e -> buscar());
        btnRefresh.addActionListener(e -> { txtBuscar.setText(""); cargarDatos(); });
    }

    // ── Datos ─────────────────────────────────────────────────────────────────

    private void cargarDatos() {
        modeloTabla.setRowCount(0);
        dao.listarTodos().forEach(this::fila);
    }

    private void buscar() {
        String txt = txtBuscar.getText().trim();
        if (txt.isEmpty()) { cargarDatos(); return; }
        modeloTabla.setRowCount(0);
        dao.buscarPorMunicipio(txt).forEach(this::fila);
    }

    private void fila(InstitucionReceptora i) {
        modeloTabla.addRow(new Object[]{
            i.getId(), i.getNombre(), i.getNit(),
            i.getMunicipio(), i.getDepartamento(),
            i.getZona(), i.isActivo() ? "Sí" : "No"
        });
    }

    private void cargarFormulario() {
        int f = tabla.getSelectedRow(); if (f < 0) return;
        int id = (int) modeloTabla.getValueAt(f, 0);
        seleccionada = dao.buscarPorId(id);
        if (seleccionada == null) return;
        txtNombre.setText(seleccionada.getNombre());
        txtNit.setText(nv(seleccionada.getNit()));
        txtDane.setText(nv(seleccionada.getDane()));
        txtDireccion.setText(seleccionada.getDireccion());
        txtMunicipio.setText(seleccionada.getMunicipio());
        txtDepartamento.setText(seleccionada.getDepartamento());
        cmbZona.setSelectedItem(seleccionada.getZona());
        String niv = nv(seleccionada.getNivelEducativo());
        chkPreescolar.setSelected(niv.contains("Preescolar"));
        chkPrimaria.setSelected(niv.contains("Primaria"));
        chkSecundaria.setSelected(niv.contains("Secundaria"));
        chkMedia.setSelected(niv.contains("Media"));
        txtRector.setText(nv(seleccionada.getNombreRector()));
        txtCorreo.setText(nv(seleccionada.getCorreoContacto()));
        txtTelefono.setText(nv(seleccionada.getTelefono()));
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    private void guardar() {
        String nombre = txtNombre.getText().trim();
        String dir    = txtDireccion.getText().trim();
        String mun    = txtMunicipio.getText().trim();
        String dep    = txtDepartamento.getText().trim();
        String nivel  = construirNiveles();

        if (nombre.isEmpty() || dir.isEmpty() || mun.isEmpty()
                || dep.isEmpty() || nivel.isEmpty()) {
            alerta("Complete los campos obligatorios (*) y seleccione al menos un nivel educativo.");
            return;
        }

        if (seleccionada == null) {
            InstitucionReceptora i = new InstitucionReceptora(
                nombre, txtNit.getText().trim(), dir, mun, dep,
                (String) cmbZona.getSelectedItem(), nivel);
            i.setDane(txtDane.getText().trim());
            i.setNombreRector(txtRector.getText().trim());
            i.setCorreoContacto(txtCorreo.getText().trim());
            i.setTelefono(txtTelefono.getText().trim());
            ok(dao.insertar(i), "Institución guardada.", "No se pudo guardar.");
        } else {
            seleccionada.setNombre(nombre);
            seleccionada.setNit(txtNit.getText().trim());
            seleccionada.setDane(txtDane.getText().trim());
            seleccionada.setDireccion(dir);
            seleccionada.setMunicipio(mun);
            seleccionada.setDepartamento(dep);
            seleccionada.setZona((String) cmbZona.getSelectedItem());
            seleccionada.setNivelEducativo(nivel);
            seleccionada.setNombreRector(txtRector.getText().trim());
            seleccionada.setCorreoContacto(txtCorreo.getText().trim());
            seleccionada.setTelefono(txtTelefono.getText().trim());
            ok(dao.actualizar(seleccionada), "Institución actualizada.", "No se pudo actualizar.");
        }
        limpiar(); cargarDatos();
    }

    private void eliminar() {
        int f = tabla.getSelectedRow();
        if (f < 0) { alerta("Seleccione una institución."); return; }
        if (JOptionPane.showConfirmDialog(this,
                "¿Desactivar la institución seleccionada?", "Confirmar",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)
                == JOptionPane.YES_OPTION) {
            ok(dao.eliminar((int) modeloTabla.getValueAt(f, 0)),
               "Institución desactivada.", "No se pudo desactivar.");
            limpiar(); cargarDatos();
        }
    }

    private void limpiar() {
        seleccionada = null;
        txtNombre.setText(""); txtNit.setText(""); txtDane.setText("");
        txtDireccion.setText(""); txtMunicipio.setText("");
        txtDepartamento.setText(""); txtRector.setText("");
        txtCorreo.setText(""); txtTelefono.setText("");
        cmbZona.setSelectedIndex(0);
        chkPreescolar.setSelected(false); chkPrimaria.setSelected(false);
        chkSecundaria.setSelected(false); chkMedia.setSelected(false);
        tabla.clearSelection();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String construirNiveles() {
        StringBuilder sb = new StringBuilder();
        if (chkPreescolar.isSelected()) { if (sb.length()>0) sb.append(","); sb.append("Preescolar"); }
        if (chkPrimaria.isSelected())   { if (sb.length()>0) sb.append(","); sb.append("Primaria"); }
        if (chkSecundaria.isSelected()) { if (sb.length()>0) sb.append(","); sb.append("Secundaria"); }
        if (chkMedia.isSelected())      { if (sb.length()>0) sb.append(","); sb.append("Media"); }
        return sb.toString();
    }

    private String nv(String s) { return s != null ? s : ""; }

    private JLabel etq(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(AZUL); l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }
    private JTextField campo(JPanel p, String etq) {
        p.add(etq(etq));
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        tf.setAlignmentX(LEFT_ALIGNMENT);
        p.add(tf); p.add(Box.createVerticalStrut(5));
        return tf;
    }
    private void estCmb(JComboBox<?> c) {
        c.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        c.setAlignmentX(LEFT_ALIGNMENT);
    }
    private JCheckBox chk(String t) {
        JCheckBox c = new JCheckBox(t);
        c.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        c.setBackground(Color.WHITE);
        return c;
    }
    private JButton btn(String t, Color c) {
        JButton b = new JButton(t);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBackground(c); b.setForeground(Color.WHITE);
        b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
    private void estilizarTabla(JTable t) {
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t.setRowHeight(26);
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        t.getTableHeader().setBackground(AZUL);
        t.getTableHeader().setForeground(Color.WHITE);
        t.setSelectionBackground(new Color(0xD6, 0xE4, 0xF0));
        t.setGridColor(new Color(0xE0, 0xE0, 0xE0));
    }
    private void alerta(String m) { JOptionPane.showMessageDialog(this, m, "Aviso",  JOptionPane.WARNING_MESSAGE); }
    private void ok(boolean r, String ok, String err) {
        if (r) JOptionPane.showMessageDialog(this, ok,  "Éxito", JOptionPane.INFORMATION_MESSAGE);
        else   JOptionPane.showMessageDialog(this, err, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
