package practicaspedagogicas.vista;

import practicaspedagogicas.modelo.Usuario;
import practicaspedagogicas.servicio.AuthServicio;
import practicaspedagogicas.vista.panels.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * MainFrame - Ventana principal del sistema.
 * Muestra un menú lateral adaptado al rol del usuario autenticado
 * y un área central donde se cargan los diferentes paneles.
 *
 * @version 1.0 - Oracle 10g XE
 */
public class MainFrame extends JFrame {

    private static final Color SIDEBAR_BG    = new Color(0x1F, 0x49, 0x7D);
    private static final Color SIDEBAR_HOVER = new Color(0x2E, 0x75, 0xB6);
    private static final Color SIDEBAR_SEL   = new Color(0x17, 0x38, 0x60);
    private static final Color SIDEBAR_TEXT  = Color.WHITE;
    private static final Color CONTENT_BG    = new Color(0xF4, 0xF7, 0xFB);

    private JPanel         contentPanel;
    private JLabel         lblUsuarioInfo;
    private JButton        btnActivo = null;   // botón de menú activo
    private final AuthServicio authServicio = new AuthServicio();

    public MainFrame(Usuario usuario) {
        super("Prácticas Pedagógicas – " + usuario.getRol());
        initUI(usuario);
    }

    private void initUI(Usuario usuario) {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1150, 720);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ── Barra superior ────────────────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(0x17, 0x38, 0x60));
        topBar.setPreferredSize(new Dimension(1150, 50));
        topBar.setBorder(new EmptyBorder(0, 20, 0, 20));

        JLabel lblApp = new JLabel("Plataforma de Prácticas Pedagógicas");
        lblApp.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblApp.setForeground(Color.WHITE);

        lblUsuarioInfo = new JLabel(usuario.getNombreCompleto() + "  |  " + usuario.getRol());
        lblUsuarioInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblUsuarioInfo.setForeground(new Color(0xBB, 0xD4, 0xEB));
        lblUsuarioInfo.setHorizontalAlignment(SwingConstants.CENTER);

        JButton btnSalir = new JButton("Cerrar sesión");
        btnSalir.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnSalir.setForeground(Color.WHITE);
        btnSalir.setBackground(new Color(0xC0, 0x39, 0x2B));
        btnSalir.setBorderPainted(false);
        btnSalir.setFocusPainted(false);
        btnSalir.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSalir.addActionListener(e -> cerrarSesion());

        topBar.add(lblApp,         BorderLayout.WEST);
        topBar.add(lblUsuarioInfo, BorderLayout.CENTER);
        topBar.add(btnSalir,       BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // ── Sidebar ───────────────────────────────────────────────────────────
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(210, 720));
        sidebar.setBorder(new EmptyBorder(16, 0, 20, 0));
        agregarItemsMenu(sidebar, usuario.getRol());
        add(sidebar, BorderLayout.WEST);

        // ── Área de contenido ─────────────────────────────────────────────────
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(CONTENT_BG);
        contentPanel.add(crearPanelBienvenida(usuario), BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);
    }

    // ── Menú lateral por rol ──────────────────────────────────────────────────

    private void agregarItemsMenu(JPanel sb, String rol) {
        if (esRol(rol, "Director", "Coordinador")) {
            separador(sb, "ADMINISTRACIÓN");
            menuBtn(sb, "🏫  Programas",     () -> cargarPanel(new ProgramaPanel()));
            menuBtn(sb, "📋  Prácticas",     () -> cargarPanel(new PracticaPanel()));
            menuBtn(sb, "👥  Usuarios",      () -> cargarPanel(new UsuarioPanel()));
            menuBtn(sb, "🏢  Instituciones", () -> cargarPanel(new InstitucionPanel()));
            menuBtn(sb, "📂  Grupos",        () -> cargarPanel(new GrupoPanel()));
        }
        if (esRol(rol, "Docente")) {
            separador(sb, "MI TRABAJO");
            menuBtn(sb, "🗓  Visitas y Horas",  () -> cargarPanel(new VisitaPanel()));
            menuBtn(sb, "📝  Evaluaciones",     () -> cargarPanel(new EvaluacionPanel()));
        }
        if (esRol(rol, "Estudiante")) {
            separador(sb, "MI PRÁCTICA");
            menuBtn(sb, "📁  Mis Actividades",  () -> cargarPanel(new ActividadPanel()));
            menuBtn(sb, "📊  Mi Evaluación",    () -> cargarPanel(new MiEvaluacionPanel()));
        }

        sb.add(Box.createVerticalGlue());

        if (esRol(rol, "Director")) {
            separador(sb, "ANÁLISIS");
            menuBtn(sb, "📈  Reportes",      () -> cargarPanel(new ReportePanel()));
        }
    }

    // ── Helpers sidebar ───────────────────────────────────────────────────────

    private void separador(JPanel sb, String texto) {
        sb.add(Box.createVerticalStrut(8));
        JLabel lbl = new JLabel("  " + texto);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(new Color(0x8A, 0xB3, 0xD9));
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        lbl.setBorder(new EmptyBorder(4, 10, 2, 0));
        sb.add(lbl);
    }

    private void menuBtn(JPanel sb, String texto, Runnable accion) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(SIDEBAR_TEXT);
        btn.setBackground(SIDEBAR_BG);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(210, 42));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 20, 10, 10));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (btn != btnActivo) btn.setBackground(SIDEBAR_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (btn != btnActivo) btn.setBackground(SIDEBAR_BG);
            }
        });

        btn.addActionListener(e -> {
            // Resetear botón anterior
            if (btnActivo != null) btnActivo.setBackground(SIDEBAR_BG);
            btnActivo = btn;
            btn.setBackground(SIDEBAR_SEL);
            accion.run();
        });

        sb.add(btn);
    }

    private boolean esRol(String actual, String... roles) {
        for (String r : roles) if (r.equalsIgnoreCase(actual)) return true;
        return false;
    }

    // ── Carga de paneles ──────────────────────────────────────────────────────

    public void cargarPanel(JPanel panel) {
        contentPanel.removeAll();
        contentPanel.add(panel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // ── Panel de bienvenida ───────────────────────────────────────────────────

    private JPanel crearPanelBienvenida(Usuario usuario) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CONTENT_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(0, 0, 10, 0);

        JLabel emoji = new JLabel("🎓");
        emoji.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 56));
        panel.add(emoji, gbc);

        gbc.gridy = 1;
        JLabel lblBienvenida = new JLabel("Bienvenido/a, " + usuario.getNombres());
        lblBienvenida.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblBienvenida.setForeground(new Color(0x1F, 0x49, 0x7D));
        panel.add(lblBienvenida, gbc);

        gbc.gridy = 2;
        JLabel lblRol = new JLabel("Rol: " + usuario.getRol()
            + "  –  Seleccione una opción del menú lateral");
        lblRol.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblRol.setForeground(Color.GRAY);
        panel.add(lblRol, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(20, 0, 0, 0);
        JLabel lblVer = new JLabel("Sistema v1.0  |  Oracle 10g XE  |  2026");
        lblVer.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblVer.setForeground(new Color(0xAA, 0xAA, 0xAA));
        panel.add(lblVer, gbc);

        return panel;
    }

    // ── Cierre de sesión ──────────────────────────────────────────────────────

    private void cerrarSesion() {
        int c = JOptionPane.showConfirmDialog(this,
            "¿Está seguro que desea cerrar sesión?",
            "Confirmar cierre de sesión", JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION) {
            authServicio.logout();
            new LoginFrame().setVisible(true);
            this.dispose();
        }
    }
}
