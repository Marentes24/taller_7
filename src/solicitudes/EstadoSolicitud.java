package solicitudes;

public enum EstadoSolicitud {
    PENDIENTE,
    EN_PROCESO,
    APROBADA,
    RECHAZADA;

    // Flujo permitido: PENDIENTE -> EN_PROCESO -> APROBADA o RECHAZADA.
    public boolean puedeCambiarA(EstadoSolicitud nuevoEstado) {
        return switch (this) {
            case PENDIENTE -> nuevoEstado == EN_PROCESO;
            case EN_PROCESO -> nuevoEstado == APROBADA || nuevoEstado == RECHAZADA;
            case APROBADA, RECHAZADA -> false;
        };
    }
}
