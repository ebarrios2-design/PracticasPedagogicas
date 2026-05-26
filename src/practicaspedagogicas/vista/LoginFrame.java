package practicaspedagogicas.vista;

import practicaspedagogicas.modelo.Usuario;
import practicaspedagogicas.servicio.AuthServicio;
import practicaspedagogicas.util.ConexionDB;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * LoginFrame - Pantalla de inicio de sesión del sistema.
 * Autentica al usuario y redirige al panel correspondiente según su rol.
 *
 * @version 1.0
 */
public class LoginFrame extends JFrame {

    // ── Componentes ───────────────────────────────────────────────────────────
    private JTextField     txtCorreo;
    private JPasswordField txtContrasena;
    private JButton        btnIngresar;
    private JLabel         lblMensaje;

    // ── Servicios ─────────────────────────────────────────────────────────────
    private final AuthServicio authServicio = new AuthServicio();

    // ── Colores institucionales ───────────────────────────────────────────────
    private static final Color AZUL_OSCURO  = new Color(0x1F, 0x49, 0x7D);
    private static final Color AZUL_MEDIO   = new Color(0x2E, 0x75, 0xB6);
    private static final Color BLANCO       = Color.WHITE;
    private static final Color GRIS_CLARO   = new Color(0xF5, 0xF5, 0xF5);

    // ── Constructor ───────────────────────────────────────────────────────────

    public LoginFrame() {
        super("Prácticas Pedagógicas – Iniciar Sesión");
        initUI();
        verificarConexion();
    }

    // ── Construcción de la interfaz ───────────────────────────────────────────

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(440, 520);
        setLocationRelativeTo(null);
        setResizable(false);

        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BLANCO);

        // ── Encabezado ────────────────────────────────────────────────────────
        JPanel headerPanel = new JPanel(new GridBagLayout());
        headerPanel.setBackground(AZUL_OSCURO);
        headerPanel.setPreferredSize(new Dimension(440, 160));

        JLabel lblTitulo = new JLabel("Prácticas Pedagógicas");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(BLANCO);

        JLabel lblSubtitulo = new JLabel("Sistema de Gestión y Seguimiento");
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitulo.setForeground(new Color(0xBB, 0xD4, 0xEB));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(0, 0, 4, 0);
        headerPanel.add(lblTitulo, gbc);
        gbc.gridy = 1;
        headerPanel.add(lblSubtitulo, gbc);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // ── Formulario ────────────────────────────────────────────────────────
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(BLANCO);
        formPanel.setBorder(new EmptyBorder(30, 50, 20, 50));

        // Correo
        JLabel lblCorreo = crearLabel("Correo institucional");
        txtCorreo = new JTextField();
        estilizarCampo(txtCorreo);

        // Contraseña
        JLabel lblPass = crearLabel("Contraseña");
        txtContrasena = new JPasswordField();
        estilizarCampo(txtContrasena);

        // Mensaje de error/estado
        lblMensaje = new JLabel(" ");
        lblMensaje.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblMensaje.setForeground(Color.RED);
        lblMensaje.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Botón ingresar
        btnIngresar = new JButton("INGRESAR AL SISTEMA");
        btnIngresar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnIngresar.setBackground(AZUL_MEDIO);
        btnIngresar.setForeground(BLANCO);
        btnIngresar.setFocusPainted(false);
        btnIngresar.setBorderPainted(false);
        btnIngresar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnIngresar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnIngresar.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Ensamblado del formulario
        formPanel.add(lblCorreo);
        formPanel.add(Box.createVerticalStrut(4));
        formPanel.add(txtCorreo);
        formPanel.add(Box.createVerticalStrut(16));
        formPanel.add(lblPass);
        formPanel.add(Box.createVerticalStrut(4));
        formPanel.add(txtContrasena);
        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(lblMensaje);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(btnIngresar);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // ── Pie de página ─────────────────────────────────────────────────────
        JLabel lblPie = new JLabel("© 2026 – Plataforma de Prácticas Pedagógicas  v1.0",
                                   SwingConstants.CENTER);
        lblPie.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblPie.setForeground(Color.GRAY);
        lblPie.setBorder(new EmptyBorder(8, 0, 12, 0));
        mainPanel.add(lblPie, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        // ── Eventos ───────────────────────────────────────────────────────────
        btnIngresar.addActionListener(e -> autenticar());

        // Enter en cualquier campo dispara el login
        KeyAdapter enterListener = new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) autenticar();
            }
        };
        txtCorreo.addKeyListener(enterListener);
        txtContrasena.addKeyListener(enterListener);

        // Efecto hover en el botón
        btnIngresar.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                btnIngresar.setBackground(AZUL_OSCURO);
            }
            @Override public void mouseExited(MouseEvent e) {
                btnIngresar.setBackground(AZUL_MEDIO);
            }
        });
    }

    // ── Lógica de autenticación ───────────────────────────────────────────────

    /**
     * Valida los campos y llama al servicio de autenticación.
     * Si es exitoso, abre el panel correspondiente al rol del usuario.
     */
    private void autenticar() {
        String correo    = txtCorreo.getText().trim();
        String contrasena = new String(txtContrasena.getPassword());

        if (correo.isEmpty() || contrasena.isEmpty()) {
            mostrarError("Por favor ingrese su correo y contraseña.");
            return;
        }

        // Deshabilitar botón durante la consulta
        btnIngresar.setEnabled(false);
        lblMensaje.setText("Verificando credenciales...");
        lblMensaje.setForeground(AZUL_MEDIO);

        // Ejecutar en hilo separado para no bloquear la UI
        SwingWorker<Usuario, Void> worker = new SwingWorker<>() {
            @Override
            protected Usuario doInBackground() {
                return authServicio.login(correo, contrasena);
            }

            @Override
            protected void done() {
                try {
                    Usuario usuario = get();
                    if (usuario != null) {
                        abrirPanelPorRol(usuario);
                    } else {
                        mostrarError("Correo o contraseña incorrectos.");
                        txtContrasena.setText("");
                        btnIngresar.setEnabled(true);
                    }
                } catch (Exception ex) {
                    mostrarError("Error de conexión con el servidor.");
                    btnIngresar.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    /**
     * Redirige al panel principal según el rol del usuario autenticado.
     */
    private void abrirPanelPorRol(Usuario usuario) {
        JFrame panelDestino;
        switch (usuario.getRol()) {
            case "Director":
            case "Coordinador":
                panelDestino = new MainFrame(usuario);
                break;
            case "Docente":
                panelDestino = new MainFrame(usuario);
                break;
            case "Estudiante":
                panelDestino = new MainFrame(usuario);
                break;
            default:
                panelDestino = new MainFrame(usuario);
        }
        panelDestino.setVisible(true);
        this.dispose();
    }

    // ── Helpers de UI ─────────────────────────────────────────────────────────

    private void mostrarError(String mensaje) {
        lblMensaje.setForeground(Color.RED);
        lblMensaje.setText(mensaje);
    }

    private JLabel crearLabel(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(AZUL_OSCURO);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private void estilizarCampo(JTextField campo) {
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        campo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xCC, 0xCC, 0xCC), 1, true),
            new EmptyBorder(8, 10, 8, 10)
        ));
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        campo.setAlignmentX(Component.LEFT_ALIGNMENT);
        campo.setBackground(GRIS_CLARO);
    }

    /** Verifica la conexión a la BD al iniciar y muestra una advertencia si falla. */
    private void verificarConexion() {
        if (!ConexionDB.probarConexion()) {
            JOptionPane.showMessageDialog(this,
                "No se pudo conectar a la base de datos.\n" +
                "Verifique que MySQL esté activo y revise el archivo db.properties.",
                "Error de conexión",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Main ──────────────────────────────────────────────────────────────────

    /**
     * Punto de entrada de la aplicación.
     */
    public static void main(String[] args) {
        // Look & Feel del sistema operativo
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}
