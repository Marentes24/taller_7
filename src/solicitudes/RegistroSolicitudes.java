package solicitudes;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

// Guarda las solicitudes registradas y es, a la vez, el modelo de la JTable que las muestra.
public class RegistroSolicitudes extends AbstractTableModel {

    private static final String[] COLUMNAS = {"Estudiante", "Código", "Tipo", "Estado", "Descripción"};

    private final List<Solicitud> solicitudes = new ArrayList<>();

    public void registrar(Solicitud nueva) throws SolicitudDuplicadaException {
        boolean duplicada = solicitudes.stream()
                .anyMatch(s -> s.getCodigo().equals(nueva.getCodigo()) && s.getTipo() == nueva.getTipo());
        if (duplicada) {
            throw new SolicitudDuplicadaException(
                    "El código " + nueva.getCodigo() + " ya tiene una solicitud de tipo " + nueva.getTipo());
        }
        solicitudes.add(nueva);
        fireTableRowsInserted(solicitudes.size() - 1, solicitudes.size() - 1);
    }

    public void cambiarEstado(int fila, EstadoSolicitud nuevoEstado) throws SolicitudInvalidaException {
        solicitudes.get(fila).cambiarEstado(nuevoEstado);
        fireTableRowsUpdated(fila, fila);
    }

    @Override
    public int getRowCount() {
        return solicitudes.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNAS.length;
    }

    @Override
    public String getColumnName(int columna) {
        return COLUMNAS[columna];
    }

    @Override
    public Object getValueAt(int fila, int columna) {
        Solicitud solicitud = solicitudes.get(fila);
        return switch (columna) {
            case 0 -> solicitud.getEstudiante();
            case 1 -> solicitud.getCodigo();
            case 2 -> solicitud.getTipo();
            case 3 -> solicitud.getEstado();
            default -> solicitud.getDescripcion();
        };
    }
}
