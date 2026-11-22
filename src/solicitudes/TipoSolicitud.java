package solicitudes;

public enum TipoSolicitud {
    CERTIFICADO("Certificado"),
    CANCELACION("Cancelación"),
    HOMOLOGACION("Homologación"),
    OTRO("Otro");

    private final String nombre;

    TipoSolicitud(String nombre) {
        this.nombre = nombre;
    }

    // El JComboBox y la JTable muestran este texto en lugar del nombre de la constante.
    @Override
    public String toString() {
        return nombre;
    }
}
