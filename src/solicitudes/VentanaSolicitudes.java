package solicitudes;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class VentanaSolicitudes extends JFrame {

    private final JTextField txtNombre = new JTextField(25);
    private final JTextField txtCodigo = new JTextField(25);
    private final JComboBox<TipoSolicitud> comboTipo = new JComboBox<>(TipoSolicitud.values());
    private final JTextArea txtDescripcion = new JTextArea(4, 25);
    private final RegistroSolicitudes registro = new RegistroSolicitudes();
    private final JTable tabla = new JTable(registro);

    public VentanaSolicitudes() {
        super("Sistema de solicitudes académicas");

        // Barra de título transparente de macOS: el contenido ocupa toda la ventana.
        getRootPane().putClientProperty("apple.awt.fullWindowContent", true);
        getRootPane().putClientProperty("apple.awt.transparentTitleBar", true);
        getRootPane().putClientProperty("apple.awt.windowTitleVisible", false);

        JLabel titulo = new JLabel("SISTEMA DE SOLICITUDES ACADÉMICAS", SwingConstants.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 20f));

        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);

        JButton btnRegistrar = new JButton("REGISTRAR SOLICITUD");
        btnRegistrar.addActionListener(e -> registrarSolicitud());
        getRootPane().setDefaultButton(btnRegistrar);

        JPanel formulario = new JPanel(new GridBagLayout());
        agregarFila(formulario, 0, "Estudiante:", txtNombre);
        agregarFila(formulario, 1, "Código:", txtCodigo);
        agregarFila(formulario, 2, "Tipo de solicitud:", comboTipo);
        agregarFila(formulario, 3, "Descripción:", new JScrollPane(txtDescripcion));
        agregarFila(formulario, 4, "", btnRegistrar);

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setPreferredScrollableViewportSize(new Dimension(680, 150));
        tabla.setShowGrid(false);
        tabla.setIntercellSpacing(new Dimension(0, 0));
        tabla.setRowHeight(24);
        tabla.setFillsViewportHeight(true);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        acciones.add(botonEstado("Pasar a en proceso", EstadoSolicitud.EN_PROCESO, "first"));
        acciones.add(botonEstado("Aprobar", EstadoSolicitud.APROBADA, "middle"));
        acciones.add(botonEstado("Rechazar", EstadoSolicitud.RECHAZADA, "last"));

        JPanel registros = new JPanel(new BorderLayout(0, 6));
        registros.setBorder(BorderFactory.createTitledBorder("Solicitudes registradas"));
        registros.add(new JScrollPane(tabla), BorderLayout.CENTER);
        registros.add(acciones, BorderLayout.SOUTH);

        JPanel encabezado = new JPanel(new BorderLayout(0, 10));
        encabezado.add(titulo, BorderLayout.NORTH);
        encabezado.add(formulario, BorderLayout.CENTER);

        JPanel contenido = new JPanel(new BorderLayout(0, 10)) {
            // Fondo difuminado: manchas de color suaves sobre el color de la ventana.
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                mancha((Graphics2D) g, 0, 0, new Color(0x5AC8FA));
                mancha((Graphics2D) g, getWidth(), getHeight() / 3, new Color(0xAF52DE));
                mancha((Graphics2D) g, getWidth() / 4, getHeight(), new Color(0x007AFF));
            }

            private void mancha(Graphics2D g, int x, int y, Color color) {
                Color transparente = new Color(color.getRGB() & 0xFFFFFF, true);
                g.setPaint(new RadialGradientPaint(x, y, Math.max(getWidth(), getHeight()) * 0.6f,
                        new float[]{0f, 1f}, new Color[]{new Color(color.getRGB() & 0xFFFFFF | 0x50000000, true), transparente}));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        formulario.setOpaque(false);
        acciones.setOpaque(false);
        registros.setOpaque(false);
        encabezado.setOpaque(false);
        contenido.setBorder(BorderFactory.createEmptyBorder(36, 16, 16, 16)); // 36: espacio para los botones de la ventana
        contenido.add(encabezado, BorderLayout.NORTH);
        contenido.add(registros, BorderLayout.CENTER);

        setContentPane(contenido);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }

    private void registrarSolicitud() {
        try {
            Solicitud solicitud = new Solicitud(
                    txtNombre.getText().trim(),
                    txtCodigo.getText().trim(),
                    txtDescripcion.getText().trim(),
                    (TipoSolicitud) comboTipo.getSelectedItem());

            registro.registrar(solicitud);

            JOptionPane.showMessageDialog(this, "Solicitud registrada correctamente",
                    "Registro exitoso", JOptionPane.INFORMATION_MESSAGE);
            limpiarFormulario();

        } catch (SolicitudInvalidaException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error de validación", JOptionPane.ERROR_MESSAGE);
        } catch (SolicitudDuplicadaException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Solicitud duplicada", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void cambiarEstado(EstadoSolicitud nuevoEstado) {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una solicitud en la tabla",
                    "Sin selección", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            registro.cambiarEstado(fila, nuevoEstado);
        } catch (SolicitudInvalidaException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Cambio de estado no permitido",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Los tres botones forman un control segmentado de macOS.
    private JButton botonEstado(String texto, EstadoSolicitud nuevoEstado, String posicion) {
        JButton boton = new JButton(texto);
        boton.putClientProperty("JButton.buttonType", "segmented");
        boton.putClientProperty("JButton.segmentPosition", posicion);
        boton.addActionListener(e -> cambiarEstado(nuevoEstado));
        return boton;
    }

    private void limpiarFormulario() {
        txtNombre.setText("");
        txtCodigo.setText("");
        txtDescripcion.setText("");
        comboTipo.setSelectedIndex(0);
        txtNombre.requestFocusInWindow();
    }

    private static void agregarFila(JPanel panel, int fila, String etiqueta, JComponent campo) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = fila;
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.BASELINE_TRAILING;
        panel.add(new JLabel(etiqueta), c);
        c.anchor = GridBagConstraints.BASELINE;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        panel.add(campo, c);
    }

    public static void main(String[] args) {
        System.setProperty("apple.awt.application.name", "Solicitudes académicas");
        // Siempre en modo claro: el aspecto Aqua de Swing no se adapta bien al modo oscuro.
        System.setProperty("apple.awt.application.appearance", "NSAppearanceNameAqua");
        Taskbar.getTaskbar().setIconImage(icono());
        SwingUtilities.invokeLater(() -> new VentanaSolicitudes().setVisible(true));
    }

    // Ícono del Dock. macOS también lo muestra en los diálogos; sin él aparece el de Java.
    private static Image icono() {
        BufferedImage imagen = new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = imagen.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setPaint(new GradientPaint(0, 50, new Color(0x5AC8FA), 0, 462, new Color(0x007AFF)));
        g.fillRoundRect(50, 50, 412, 412, 92, 92);
        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 200));
        FontMetrics m = g.getFontMetrics();
        g.drawString("SA", (512 - m.stringWidth("SA")) / 2, (512 - m.getHeight()) / 2 + m.getAscent());
        g.dispose();
        return imagen;
    }
}
