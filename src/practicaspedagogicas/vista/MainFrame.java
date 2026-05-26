package practicaspedagogicas.vista;

import practicaspedagogicas.modelo.Usuario;
import practicaspedagogicas.servicio.AuthServicio;
import practicaspedagogicas.util.SesionUsuario;
import practicaspedagogicas.vista.panels.ProgramaPanel;
import practicaspedagogicas.vista.panels.PracticaPanel;
import practicaspedagogicas.vista.panels.UsuarioPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * MainFrame - Ventana principal del sistema.
 * Muestra un menú lateral adaptado al rol del usuario autenticado
 * y un área central donde se cargan los diferentes paneles.
 *
 * @version 1.0
 */
public class MainFrame extends JFrame {

    // ── Constantes de diseño ─────────────────────────────────────────────────
    private static final Color SIDEBAR_BG   = new Color(0x1F, 0x49, 0x7D);
    private static final Color SIDEBAR_HOVER= new Color(0x2E, 0x75, 0xB6);
    private static final Color SIDEBAR_TEXT = Color.WHITE;
    private static final Color CONTENT_BG   = new Color(0xF4, 0xF7, 0xFB);

    // ── Componentes ───────────────────────────────────────────────────────────
    private JPanel     contentPanel;
    private JLabel     lblUsuarioInfo;
    private final AuthServicio authServicio = new AuthServicio();

    public MainFrame(Usuario usuario) {
        super("Prácticas Pedagógicas – " + usuario.getRol());
        initUI(usuario);
    }

    private void initUI(Usuario usuario) {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ── Barra superior ────────────────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(0x17, 0x38, 0x60));
        topBar.setPreferredSize(new Dimension(1100, 50));
        topBar.setBorder(new EmptyBorder(0, 20, 0, 20));

        JLabel lblApp = new JLabel("Plataforma de Prácticas Pedagógicas");
        lblApp.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblApp.setForeground(Color.WHITE);

        lblUsuarioInfo = new JLabel(
            usuario.getNombreCompleto() + "  |  " + usuario.getRol());
        lblUsuarioInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblUsuarioInfo.setForeground(new Color(0xBB, 0xD4, 0xEB));

        JButton btnSalir = new JButton("Cerrar sesión");
        btnSalir.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnSalir.setForeground(Color.WHITE);
        btnSalir.setBackground(new Color(0xC0, 0x39, 0x2B));
        btnSalir.setBorderPainted(false);
        btnSalir.setFocusPainted(false);
        btnSalir.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSalir.addActionListener(e -> cerrarSesion());

        topBar.add(lblApp, BorderLayout.WEST);
        topBar.add(lblUsuarioInfo, BorderLayout.CENTER);
        topBar.add(btnSalir, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // ── Sidebar ───────────────────────────────────────────────────────────
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(200, 700));
        sidebar.setBorder(new EmptyBorder(20, 0, 20, 0));

        // Menú según rol
        agregarItemsMenu(sidebar, usuario.getRol());
        add(sidebar, BorderLayout.WEST);

        // ── Área de contenido ─────────────────────────────────────────────────
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(CONTENT_BG);

        // Panel de bienvenida inicial
        JPanel bienvenida = crearPanelBienvenida(usuario);
        contentPanel.add(bienvenida, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);
    }

    // ── Menú lateral según rol ────────────────────────────────────────────────

    private void agregarItemsMenu(JPanel sidebar, String rol) {
        // Módulos disponibles para todos los roles con acceso
        if (esRol(rol, "Director", "Coordinador")) {
            agregarMenuBtn(sidebar, "🏫  Programas",    () -> cargarPanel(new ProgramaPanel()));
            agregarMenuBtn(sidebar, "📋  Prácticas",    () -> cargarPanel(new PracticaPanel()));
            agregarMenuBtn(sidebar, "👥  Usuarios",     () -> cargarPanel(new UsuarioPanel()));
            agregarMenuBtn(sidebar, "🏢  Instituciones",() -> mostrarProximamente("Instituciones"));
            agregarMenuBtn(sidebar, "📂  Grupos",       () -> mostrarProximamente("Grupos"));
        }
        if (esRol(rol, "Docente")) {
            agregarMenuBtn(sidebar, "📋  Mis grupos",    () -> mostrarProximamente("Mis Grupos"));
            agregarMenuBtn(sidebar, "📝  Evaluaciones",  () -> mostrarProximamente("Evaluaciones"));
            agregarMenuBtn(sidebar, "🗓  Visitas",       () -> mostrarProximamente("Visitas"));
        }
        if (esRol(rol, "Estudiante")) {
            agregarMenuBtn(sidebar, "📋  Mi práctica",   () -> mostrarProximamente("Mi Práctica"));
            agregarMenuBtn(sidebar, "📁  Actividades",   () -> mostrarProximamente("Actividades"));
            agregarMenuBtn(sidebar, "📊  Mi evaluación", () -> mostrarProximamente("Mi Evaluación"));
        }

        sidebar.add(Box.createVerticalGlue());

        if (esRol(rol, "Director")) {
            agregarMenuBtn(sidebar, "📈  Reportes",     () -> mostrarProximamente("Reportes"));
        }
        agregarMenuBtn(sidebar, "⚙   Configuración", () -> mostrarProximamente("Configuración"));
    }

    // ── Helpers de sidebar ────────────────────────────────────────────────────

    private void agregarMenuBtn(JPanel sidebar, String texto, Runnable accion) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(SIDEBAR_TEXT);
        btn.setBackground(SIDEBAR_BG);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(200, 42));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 20, 10, 10));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(SIDEBAR_HOVER); }
            public void mouseExited (java.awt.event.MouseEvent e) { btn.setBackground(SIDEBAR_BG); }
        });
        btn.addActionListener(e -> accion.run());
        sidebar.add(btn);
    }

    private boolean esRol(String rolActual, String... roles) {
        for (String r : roles) if (r.equalsIgnoreCase(rolActual)) return true;
        return false;
    }

    // ── Carga de paneles ──────────────────────────────────────────────────────

    /**
     * Reemplaza el contenido central por el panel indicado.
     */
    public void cargarPanel(JPanel panel) {
        contentPanel.removeAll();
        contentPanel.add(panel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void mostrarProximamente(String modulo) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CONTENT_BG);
        JLabel lbl = new JLabel("Módulo \"" + modulo + "\" – Próximamente");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lbl.setForeground(new Color(0x2E, 0x75, 0xB6));
        panel.add(lbl);
        cargarPanel(panel);
    }

    // ── Panel de bienvenida ───────────────────────────────────────────────────

    private JPanel crearPanelBienvenida(Usuario usuario) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CONTENT_BG);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(0, 0, 8, 0);

        JLabel emoji = new JLabel("🎓");
        emoji.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 52));
        panel.add(emoji, gbc);

        gbc.gridy = 1;
        JLabel lblBienvenida = new JLabel("Bienvenido/a, " + usuario.getNombres());
        lblBienvenida.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblBienvenida.setForeground(new Color(0x1F, 0x49, 0x7D));
        panel.add(lblBienvenida, gbc);

        gbc.gridy = 2;
        JLabel lblRol = new JLabel("Rol: " + usuario.getRol() + "  –  Seleccione una opción del menú lateral");
        lblRol.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblRol.setForeground(Color.GRAY);
        panel.add(lblRol, gbc);

        return panel;
    }

    // ── Cierre de sesión ──────────────────────────────────────────────────────

    private void cerrarSesion() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Está seguro que desea cerrar sesión?",
            "Confirmar cierre de sesión",
            JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            authServicio.logout();
            new LoginFrame().setVisible(true);
            this.dispose();
        }
    }
}
