import solicitudes.EstadoSolicitud;
import solicitudes.TipoSolicitud;
import solicitudes.VentanaSolicitudes;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

// Herramienta del informe (no es parte de la entrega): abre la aplicación, la maneja como lo haría un usuario
// y toma una captura de pantalla real (macOS screencapture) de cada escenario.
public class Evidencias {

    private static File carpeta;
    private static JFrame ventana;
    private static JTextField nombre, codigo;
    private static JTextArea descripcion;
    private static JComboBox<?> tipo;
    private static JTable tabla;

    public static void main(String[] args) throws Exception {
        carpeta = new File(args[0]);
        carpeta.mkdirs();
        VentanaSolicitudes.main(new String[0]);
        SwingUtilities.invokeAndWait(() -> {
            ventana = (JFrame) Arrays.stream(Frame.getFrames()).filter(VentanaSolicitudes.class::isInstance).findFirst().orElseThrow();
            ventana.setAlwaysOnTop(true);
            nombre = buscar(JTextField.class).get(0);
            codigo = buscar(JTextField.class).get(1);
            descripcion = buscar(JTextArea.class).get(0);
            tipo = buscar(JComboBox.class).get(0);
            tabla = buscar(JTable.class).get(0);
        });

        capturar("00_ventana_inicial");
        registrar("Laura Gómez", "202310045", TipoSolicitud.CERTIFICADO, "Certificado de notas del semestre 2026-1", "01_registro_valido");
        registrar("Carlos Ruiz", "201920331", TipoSolicitud.HOMOLOGACION, "Homologación de Cálculo I cursado en otra universidad", null);
        registrar("Ana Torres", "202410876", TipoSolicitud.CANCELACION, "Cancelación de la asignatura Física II", null);
        registrar("Laura Gómez", "202310045", TipoSolicitud.CANCELACION, "Cancelación de Inglés III por cruce de horario", "02_mismo_codigo_otro_tipo");
        registrar("", "202110458", TipoSolicitud.OTRO, "Cambio de grupo", "03_nombre_vacio");
        registrar("Pedro Díaz", "20A1", TipoSolicitud.OTRO, "Cambio de grupo", "04_codigo_invalido");
        registrar("Pedro Díaz", "202110458", TipoSolicitud.OTRO, "   ", "05_descripcion_vacia");
        registrar("Laura Gómez", "202310045", TipoSolicitud.CERTIFICADO, "Otro certificado de notas", "06_duplicada");

        cambiar(0, "Pasar a EN_PROCESO");
        capturar("07_en_proceso");
        cambiar(0, "Aprobar");
        cambiar(1, "Pasar a EN_PROCESO");
        cambiar(2, "Pasar a EN_PROCESO");
        cambiar(2, "Rechazar");
        capturar("08_aprobada_rechazada");
        SwingUtilities.invokeAndWait(() -> tabla.setRowSelectionInterval(3, 3));
        clicConDialogo("Aprobar", "09_cambio_no_permitido");
        SwingUtilities.invokeAndWait(() -> tabla.clearSelection());
        clicConDialogo("Rechazar", "10_sin_seleccion");

        List<Object> estados = IntStream.range(0, tabla.getRowCount()).mapToObj(f -> tabla.getValueAt(f, 3)).toList();
        List<Object> esperados = List.of(EstadoSolicitud.APROBADA, EstadoSolicitud.EN_PROCESO, EstadoSolicitud.RECHAZADA, EstadoSolicitud.PENDIENTE);
        if (!estados.equals(esperados)) {
            throw new AssertionError("Estados finales " + estados + ", se esperaba " + esperados);
        }
        System.out.println("Evidencias generadas en " + carpeta + " - estados finales " + estados);
        System.exit(0);
    }

    private static void registrar(String est, String cod, TipoSolicitud tipoSolicitud, String desc, String archivo) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            nombre.setText(est);
            codigo.setText(cod);
            tipo.setSelectedItem(tipoSolicitud);
            descripcion.setText(desc);
        });
        clicConDialogo("REGISTRAR SOLICITUD", archivo);
    }

    private static void cambiar(int fila, String boton) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            tabla.setRowSelectionInterval(fila, fila);
            boton(boton).doClick();
        });
    }

    // El JOptionPane es modal: se pulsa el botón sin esperar, se captura la pantalla y luego se cierra el diálogo.
    private static void clicConDialogo(String texto, String archivo) throws Exception {
        SwingUtilities.invokeLater(() -> boton(texto).doClick());
        Thread.sleep(600);
        if (archivo != null) {
            capturar(archivo);
        }
        SwingUtilities.invokeAndWait(() -> Arrays.stream(Window.getWindows())
                .filter(w -> w instanceof JDialog && w.isShowing())
                .forEach(Window::dispose));
    }

    private static void capturar(String archivo) throws Exception {
        Thread.sleep(400); // deja que la ventana termine de pintarse
        Rectangle r = ventana.getBounds();
        String region = "-R" + r.x + "," + r.y + "," + r.width + "," + r.height;
        int salida = new ProcessBuilder("screencapture", "-x", region, new File(carpeta, archivo + ".png").getPath())
                .inheritIO().start().waitFor();
        if (salida != 0) {
            throw new IllegalStateException("screencapture terminó con código " + salida);
        }
    }

    private static JButton boton(String texto) {
        return buscar(JButton.class).stream().filter(b -> texto.equals(b.getText())).findFirst().orElseThrow();
    }

    private static <T> List<T> buscar(Class<T> clase) {
        return componentes(ventana.getContentPane()).filter(clase::isInstance).map(clase::cast).toList();
    }

    private static Stream<Component> componentes(Container contenedor) {
        return Arrays.stream(contenedor.getComponents())
                .flatMap(c -> Stream.concat(Stream.of(c), c instanceof Container k ? componentes(k) : Stream.empty()));
    }
}
