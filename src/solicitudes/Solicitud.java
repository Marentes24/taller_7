package solicitudes;

public class Solicitud {

    private final String estudiante;
    private final String codigo;
    private final String descripcion;
    private final TipoSolicitud tipo;
    private EstadoSolicitud estado;

    public Solicitud(String estudiante, String codigo, String descripcion, TipoSolicitud tipo)
            throws SolicitudInvalidaException {

        if (estudiante == null || estudiante.trim().isEmpty()) {
            throw new SolicitudInvalidaException("El nombre es obligatorio");
        }
        if (codigo == null || !codigo.matches("\\d{5,12}")) {
            throw new SolicitudInvalidaException("El código debe tener entre 5 y 12 dígitos");
        }
        if (tipo == null) {
            throw new SolicitudInvalidaException("El tipo de solicitud es obligatorio");
        }
        if (descripcion == null || descripcion.trim().isEmpty()) {
            throw new SolicitudInvalidaException("La descripción es obligatoria");
        }

        this.estudiante = estudiante;
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.estado = EstadoSolicitud.PENDIENTE;
    }

    public void cambiarEstado(EstadoSolicitud nuevoEstado) throws SolicitudInvalidaException {
        if (!estado.puedeCambiarA(nuevoEstado)) {
            throw new SolicitudInvalidaException(
                    "Una solicitud " + estado + " no puede pasar a " + nuevoEstado);
        }
        estado = nuevoEstado;
    }

    public String getEstudiante() {
        return estudiante;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public TipoSolicitud getTipo() {
        return tipo;
    }

    public EstadoSolicitud getEstado() {
        return estado;
    }

    @Override
    public String toString() {
        return estudiante + " | " + codigo + " | " + tipo + " | " + estado;
    }
}
