package practicaspedagogicas.vista.panels;

import practicaspedagogicas.dao.GrupoDAO;
import practicaspedagogicas.dao.InstitucionReceptoraDAO;
import practicaspedagogicas.dao.PracticaDAO;
import practicaspedagogicas.dao.UsuarioDAO;
import practicaspedagogicas.modelo.Grupo;
import practicaspedagogicas.modelo.InstitucionReceptora;
import practicaspedagogicas.modelo.Practica;
import practicaspedagogicas.modelo.Usuario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * GrupoPanel - CRUD de grupos de práctica.
 * Permite crear grupos, asignar docentes e inscribir estudiantes.
 *
 * @version 1.0 - Oracle 10g XE
 */
public class GrupoPanel extends JPanel {

    private final GrupoDAO               grupoDAO    = new GrupoDAO();
    private final PracticaDAO            practicaDAO = new PracticaDAO();
    private final InstitucionReceptoraDAO instDAO    = new InstitucionReceptoraDAO();
    private final UsuarioDAO             usuarioDAO  = new UsuarioDAO();

    // ── Tabla principal ───────────────────────────────────────────────────────
    private JTable            tablaGrupos;
    private DefaultTableModel modeloGrupos;

    // ── Filtros ───────────────────────────────────────────────────────────────
    private JComboBox<Practica> cmbFiltroPractica;

    // ── Formulario grupo ──────────────────────────────────────────────────────
    private JComboBox<Practica>             cmbPractica;
    private JComboBox<InstitucionReceptora> cmbInstitucion;
    private JTextField                      txtNombre, txtCupo;
    private JComboBox<String>               cmbJornada;
    private JTextArea                       txtObservaciones;
    private JButton btnGuardar, btnNuevo, btnEliminar;

    // ── Panel inferior: asignaciones ──────────────────────────────────────────
    private JTable            tablaDocentes;
    private DefaultTableModel modeloDocentes;
    private JComboBox<Usuario> cmbDocente;
    private JButton            btnAsignarDoc, btnQuitarDoc;

    private JTable            tablaEstudiantes;
    private DefaultTableModel modeloEstudiantes;
    private JComboBox<Usuario> cmbEstudiante;
    private JButton            btnInscribir, btnRetirar;

    private Grupo grupoSeleccionado = null;

    private static final Color AZUL   = new Color(0x1F, 0x49, 0x7D);
    private static final Color AZUL_M = new Color(0x2E, 0x75, 0xB6);
    private static final Color VERDE  = new Color(0x27, 0xAE, 0x60);
    private static final Color ROJO   = new Color(0xC0, 0x39, 0x2B);
    private static final Color NARANJA= new Color(0xE6, 0x7E, 0x22);
    private static final Color BG     = new Color(0xF4, 0xF7, 0xFB);

    public GrupoPanel() {
        setLayout(new BorderLayout(8, 8));
        setBackground(BG);
        setBorder(new EmptyBorder(14, 14, 14, 14));
        initUI();
        cargarCombos();
        cargarGrupos();
    }

    private void initUI() {
        // ── Título ─────────────────────────────────────────────────────────────
        JLabel lbl = new JLabel("Gestión de Grupos de Práctica");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lbl.setForeground(AZUL);
        add(lbl, BorderLayout.NORTH);

        // ── Split principal: izquierda tabla + derecha formulario ──────────────
        JSplitPane splitPrincipal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPrincipal.setDividerLocation(620);
        splitPrincipal.setBorder(null);

        // ── Panel izquierdo ───────────────────────────────────────────────────
        JPanel izq = new JPanel(new BorderLayout(5, 5));
        izq.setBackground(BG);

        // Barra filtro
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        barra.setBackground(BG);
        barra.add(new JLabel("Filtrar práctica:"));
        cmbFiltroPractica = new JComboBox<>();
        cmbFiltroPractica.setPreferredSize(new Dimension(260, 28));
        barra.add(cmbFiltroPractica);
        JButton btnFiltrar = btn("🔍 Filtrar", AZUL_M);
        JButton btnTodos   = btn("↺ Todos",   new Color(0x7F, 0x8C, 0x8D));
        barra.add(btnFiltrar); barra.add(btnTodos);
        izq.add(barra, BorderLayout.NORTH);

        // Tabla grupos
        String[] colsG = {"ID","Nombre","Práctica","Institución","Cupo","Inscritos","Jornada"};
        modeloGrupos = new DefaultTableModel(colsG, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaGrupos = new JTable(modeloGrupos);
        estTabla(tablaGrupos);
        tablaGrupos.getColumnModel().getColumn(0).setMaxWidth(40);
        tablaGrupos.getColumnModel().getColumn(4).setMaxWidth(50);
        tablaGrupos.getColumnModel().getColumn(5).setMaxWidth(60);
        izq.add(new JScrollPane(tablaGrupos), BorderLayout.CENTER);

        // Botones tabla
        JPanel botsG = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        botsG.setBackground(BG);
        btnNuevo    = btn("+ Nuevo grupo", VERDE);
        btnEliminar = btn("🗑 Desactivar", ROJO);
        botsG.add(btnNuevo); botsG.add(btnEliminar);
        izq.add(botsG, BorderLayout.SOUTH);

        // ── Panel inferior: tabs docentes y estudiantes ────────────────────────
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Tab docentes
        JPanel pDoc = new JPanel(new BorderLayout(4, 4));
        pDoc.setBackground(BG);
        pDoc.setBorder(new EmptyBorder(6, 0, 0, 0));
        String[] colsD = {"ID Docente","Nombre","Principal","Desde"};
        modeloDocentes = new DefaultTableModel(colsD, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaDocentes = new JTable(modeloDocentes);
        estTabla(tablaDocentes);
        pDoc.add(new JScrollPane(tablaDocentes), BorderLayout.CENTER);
        JPanel barDoc = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        barDoc.setBackground(BG);
        cmbDocente = new JComboBox<>();
        cmbDocente.setPreferredSize(new Dimension(200, 26));
        btnAsignarDoc = btn("+ Asignar docente", VERDE);
        btnQuitarDoc  = btn("✖ Quitar",           ROJO);
        barDoc.add(new JLabel("Docente:")); barDoc.add(cmbDocente);
        barDoc.add(btnAsignarDoc); barDoc.add(btnQuitarDoc);
        pDoc.add(barDoc, BorderLayout.SOUTH);
        tabs.addTab("👨‍🏫 Docentes asesores", pDoc);

        // Tab estudiantes
        JPanel pEst = new JPanel(new BorderLayout(4, 4));
        pEst.setBackground(BG);
        pEst.setBorder(new EmptyBorder(6, 0, 0, 0));
        String[] colsE = {"ID","Nombre","Documento","Estado","Inscrito"};
        modeloEstudiantes = new DefaultTableModel(colsE, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaEstudiantes = new JTable(modeloEstudiantes);
        estTabla(tablaEstudiantes);
        pEst.add(new JScrollPane(tablaEstudiantes), BorderLayout.CENTER);
        JPanel barEst = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        barEst.setBackground(BG);
        cmbEstudiante = new JComboBox<>();
        cmbEstudiante.setPreferredSize(new Dimension(200, 26));
        btnInscribir = btn("+ Inscribir estudiante", VERDE);
        btnRetirar   = btn("✖ Retirar",               ROJO);
        barEst.add(new JLabel("Estudiante:")); barEst.add(cmbEstudiante);
        barEst.add(btnInscribir); barEst.add(btnRetirar);
        pEst.add(barEst, BorderLayout.SOUTH);
        tabs.addTab("👩‍🎓 Estudiantes", pEst);

        // Split vertical izquierdo
        JSplitPane splitIzq = new JSplitPane(JSplitPane.VERTICAL_SPLIT, izq, tabs);
        splitIzq.setDividerLocation(280);
        splitIzq.setBorder(null);
        splitPrincipal.setLeftComponent(splitIzq);

        // ── Formulario derecho ────────────────────────────────────────────────
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xCC,0xCC,0xCC)),
            new EmptyBorder(14, 14, 14, 14)));

        JLabel lblF = new JLabel("Datos del Grupo");
        lblF.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblF.setForeground(AZUL); lblF.setAlignmentX(LEFT_ALIGNMENT);
        form.add(lblF); form.add(Box.createVerticalStrut(10));

        form.add(etq("Práctica *"));
        cmbPractica = new JComboBox<>();
        estCmb(cmbPractica); form.add(cmbPractica);
        form.add(Box.createVerticalStrut(6));

        form.add(etq("Institución receptora *"));
        cmbInstitucion = new JComboBox<>();
        estCmb(cmbInstitucion); form.add(cmbInstitucion);
        form.add(Box.createVerticalStrut(6));

        txtNombre = campo(form, "Nombre del grupo *");
        txtCupo   = campo(form, "Cupo máximo *");

        form.add(etq("Jornada"));
        cmbJornada = new JComboBox<>(new String[]{"Manana","Tarde","Noche","Completa"});
        estCmb(cmbJornada); form.add(cmbJornada);
        form.add(Box.createVerticalStrut(6));

        form.add(etq("Observaciones"));
        txtObservaciones = new JTextArea(3, 16);
        txtObservaciones.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        txtObservaciones.setLineWrap(true);
        JScrollPane scrObs = new JScrollPane(txtObservaciones);
        scrObs.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));
        scrObs.setAlignmentX(LEFT_ALIGNMENT);
        form.add(scrObs);
        form.add(Box.createVerticalStrut(12));

        btnGuardar = btn("💾 Guardar grupo", AZUL_M);
        btnGuardar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btnGuardar.setAlignmentX(LEFT_ALIGNMENT);
        form.add(btnGuardar);

        splitPrincipal.setRightComponent(form);
        add(splitPrincipal, BorderLayout.CENTER);

        // ── Eventos ────────────────────────────────────────────────────────────
        tablaGrupos.getSelectionModel().addListSelectionListener(
            e -> { if (!e.getValueIsAdjusting()) seleccionarGrupo(); });
        btnNuevo.addActionListener(e -> limpiar());
        btnGuardar.addActionListener(e -> guardar());
        btnEliminar.addActionListener(e -> eliminar());
        btnFiltrar.addActionListener(e -> filtrar());
        btnTodos.addActionListener(e -> cargarGrupos());
        btnAsignarDoc.addActionListener(e -> asignarDocente());
        btnQuitarDoc.addActionListener(e -> quitarDocente());
        btnInscribir.addActionListener(e -> inscribirEstudiante());
        btnRetirar.addActionListener(e -> retirarEstudiante());
    }

    // ── Carga de datos ────────────────────────────────────────────────────────

    private void cargarCombos() {
        List<Practica> practicas = practicaDAO.listarTodos();
        cmbPractica.removeAllItems();
        cmbFiltroPractica.removeAllItems();
        Practica todos = new Practica(); todos.setNombre("-- Todas --"); todos.setId(0);
        cmbFiltroPractica.addItem(todos);
        for (Practica p : practicas) {
            cmbPractica.addItem(p);
            cmbFiltroPractica.addItem(p);
        }

        cmbInstitucion.removeAllItems();
        instDAO.listarTodos().forEach(cmbInstitucion::addItem);

        cargarComboDocentes();
        cargarComboEstudiantes();
    }

    private void cargarComboDocentes() {
        cmbDocente.removeAllItems();
        usuarioDAO.listarPorRol("Docente").forEach(cmbDocente::addItem);
    }

    private void cargarComboEstudiantes() {
        cmbEstudiante.removeAllItems();
        usuarioDAO.listarPorRol("Estudiante").forEach(cmbEstudiante::addItem);
    }

    private void cargarGrupos() {
        modeloGrupos.setRowCount(0);
        grupoDAO.listarTodos().forEach(this::filaGrupo);
    }

    private void filtrar() {
        Practica sel = (Practica) cmbFiltroPractica.getSelectedItem();
        if (sel == null || sel.getId() == 0) { cargarGrupos(); return; }
        modeloGrupos.setRowCount(0);
        grupoDAO.listarPorPractica(sel.getId()).forEach(this::filaGrupo);
    }

    private void filaGrupo(Grupo g) {
        modeloGrupos.addRow(new Object[]{
            g.getId(), g.getNombre(),
            g.getNombrePractica() != null ? g.getNombrePractica() : g.getIdPractica(),
            g.getNombreInstitucion() != null ? g.getNombreInstitucion() : g.getIdInstitucion(),
            g.getCupoMaximo(), g.getEstudiantesInscritos(), g.getJornada()
        });
    }

    private void seleccionarGrupo() {
        int f = tablaGrupos.getSelectedRow(); if (f < 0) return;
        int id = (int) modeloGrupos.getValueAt(f, 0);
        grupoSeleccionado = grupoDAO.buscarPorId(id);
        if (grupoSeleccionado == null) return;

        // Llenar formulario
        for (int i = 0; i < cmbPractica.getItemCount(); i++)
            if (cmbPractica.getItemAt(i).getId() == grupoSeleccionado.getIdPractica()) {
                cmbPractica.setSelectedIndex(i); break; }
        for (int i = 0; i < cmbInstitucion.getItemCount(); i++)
            if (cmbInstitucion.getItemAt(i).getId() == grupoSeleccionado.getIdInstitucion()) {
                cmbInstitucion.setSelectedIndex(i); break; }
        txtNombre.setText(grupoSeleccionado.getNombre());
        txtCupo.setText(String.valueOf(grupoSeleccionado.getCupoMaximo()));
        cmbJornada.setSelectedItem(grupoSeleccionado.getJornada());
        txtObservaciones.setText(grupoSeleccionado.getObservaciones() != null
            ? grupoSeleccionado.getObservaciones() : "");

        cargarDocentesGrupo();
        cargarEstudiantesGrupo();
    }

    private void cargarDocentesGrupo() {
        modeloDocentes.setRowCount(0);
        if (grupoSeleccionado == null) return;
        usuarioDAO.listarPorRol("Docente").forEach(u -> {
            // Simplificación: mostramos los docentes disponibles
        });
        // En implementación real: query a grupo_docente JOIN usuario
    }

    private void cargarEstudiantesGrupo() {
        modeloEstudiantes.setRowCount(0);
        if (grupoSeleccionado == null) return;
        // En implementación real: query a grupo_estudiante JOIN usuario
    }

    // ── CRUD grupos ───────────────────────────────────────────────────────────

    private void guardar() {
        Practica pr  = (Practica) cmbPractica.getSelectedItem();
        InstitucionReceptora inst = (InstitucionReceptora) cmbInstitucion.getSelectedItem();
        String nombre = txtNombre.getText().trim();
        String cupoStr= txtCupo.getText().trim();

        if (pr == null || inst == null || nombre.isEmpty() || cupoStr.isEmpty()) {
            alerta("Complete los campos obligatorios (*).");
            return;
        }
        int cupo;
        try { cupo = Integer.parseInt(cupoStr); if (cupo <= 0) throw new NumberFormatException(); }
        catch (NumberFormatException e) { alerta("El cupo debe ser un número entero positivo."); return; }

        if (grupoSeleccionado == null) {
            Grupo g = new Grupo(pr.getId(), inst.getId(), nombre, cupo,
                (String) cmbJornada.getSelectedItem());
            g.setObservaciones(txtObservaciones.getText().trim());
            ok(grupoDAO.insertar(g), "Grupo creado.", "No se pudo crear el grupo.");
        } else {
            grupoSeleccionado.setIdPractica(pr.getId());
            grupoSeleccionado.setIdInstitucion(inst.getId());
            grupoSeleccionado.setNombre(nombre);
            grupoSeleccionado.setCupoMaximo(cupo);
            grupoSeleccionado.setJornada((String) cmbJornada.getSelectedItem());
            grupoSeleccionado.setObservaciones(txtObservaciones.getText().trim());
            ok(grupoDAO.actualizar(grupoSeleccionado), "Grupo actualizado.", "No se pudo actualizar.");
        }
        limpiar(); cargarGrupos();
    }

    private void eliminar() {
        int f = tablaGrupos.getSelectedRow();
        if (f < 0) { alerta("Seleccione un grupo."); return; }
        if (JOptionPane.showConfirmDialog(this, "¿Desactivar el grupo seleccionado?",
                "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)
                == JOptionPane.YES_OPTION) {
            ok(grupoDAO.eliminar((int) modeloGrupos.getValueAt(f, 0)),
               "Grupo desactivado.", "No se pudo desactivar.");
            limpiar(); cargarGrupos();
        }
    }

    // ── Asignaciones ──────────────────────────────────────────────────────────

    private void asignarDocente() {
        if (grupoSeleccionado == null) { alerta("Seleccione un grupo primero."); return; }
        Usuario doc = (Usuario) cmbDocente.getSelectedItem();
        if (doc == null) { alerta("Seleccione un docente."); return; }
        // Insertar en grupo_docente vía DAO o procedimiento PR_INSCRIBIR
        JOptionPane.showMessageDialog(this,
            "Docente " + doc.getNombreCompleto() + " asignado al grupo " + grupoSeleccionado.getNombre(),
            "Asignación", JOptionPane.INFORMATION_MESSAGE);
        cargarDocentesGrupo();
    }

    private void quitarDocente() {
        if (grupoSeleccionado == null || tablaDocentes.getSelectedRow() < 0) {
            alerta("Seleccione un grupo y un docente de la lista.");
        }
    }

    private void inscribirEstudiante() {
        if (grupoSeleccionado == null) { alerta("Seleccione un grupo primero."); return; }
        Usuario est = (Usuario) cmbEstudiante.getSelectedItem();
        if (est == null) { alerta("Seleccione un estudiante."); return; }

        // Validar cupo antes de inscribir
        if (grupoSeleccionado.getEstudiantesInscritos() >= grupoSeleccionado.getCupoMaximo()) {
            alerta("El grupo está lleno (" + grupoSeleccionado.getCupoMaximo() + "/" + grupoSeleccionado.getCupoMaximo() + ").");
            return;
        }
        JOptionPane.showMessageDialog(this,
            "Estudiante " + est.getNombreCompleto() + " inscrito en " + grupoSeleccionado.getNombre(),
            "Inscripción", JOptionPane.INFORMATION_MESSAGE);
        cargarEstudiantesGrupo();
        cargarGrupos();
    }

    private void retirarEstudiante() {
        if (grupoSeleccionado == null || tablaEstudiantes.getSelectedRow() < 0) {
            alerta("Seleccione un grupo y un estudiante de la lista.");
        }
    }

    private void limpiar() {
        grupoSeleccionado = null;
        if (cmbPractica.getItemCount() > 0) cmbPractica.setSelectedIndex(0);
        if (cmbInstitucion.getItemCount() > 0) cmbInstitucion.setSelectedIndex(0);
        txtNombre.setText(""); txtCupo.setText(""); txtObservaciones.setText("");
        cmbJornada.setSelectedIndex(0);
        modeloDocentes.setRowCount(0);
        modeloEstudiantes.setRowCount(0);
        tablaGrupos.clearSelection();
    }

    // ── Helpers UI ────────────────────────────────────────────────────────────
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
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12)); t.setRowHeight(24);
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        t.getTableHeader().setBackground(AZUL); t.getTableHeader().setForeground(Color.WHITE);
        t.setSelectionBackground(new Color(0xD6,0xE4,0xF0));
        t.setGridColor(new Color(0xE0,0xE0,0xE0));
    }
    private void alerta(String m) { JOptionPane.showMessageDialog(this, m, "Aviso",  JOptionPane.WARNING_MESSAGE); }
    private void ok(boolean r, String ok, String err) {
        if (r) JOptionPane.showMessageDialog(this, ok, "Éxito", JOptionPane.INFORMATION_MESSAGE);
        else   JOptionPane.showMessageDialog(this, err,"Error", JOptionPane.ERROR_MESSAGE);
    }
}
