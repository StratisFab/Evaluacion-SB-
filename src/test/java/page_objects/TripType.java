package page_objects;

import java.util.Arrays;

/**
 * Tipos de viaje disponibles en el buscador de vuelos.
 * Relaciona la etiqueta en español usada en los escenarios Gherkin con el
 * nombre accesible que muestra el sitio (en inglés).
 */
public enum TripType {
    ROUND_TRIP("Viaje de Ida y Vuelta", "Round Trip"),
    ONE_WAY("Solo Ida", "One Way"),
    MULTI_CITY("Multidestino", "Multi-City");

    private final String spanishLabel;
    private final String accessibleName;

    TripType(String spanishLabel, String accessibleName) {
        this.spanishLabel = spanishLabel;
        this.accessibleName = accessibleName;
    }

    public String accessibleName() {
        return accessibleName;
    }

    /** Acepta la etiqueta en español, en inglés o el nombre del enum (sin distinguir mayúsculas). */
    public static TripType fromLabel(String label) {
        return Arrays.stream(values())
                .filter(t -> t.spanishLabel.equalsIgnoreCase(label.trim())
                        || t.accessibleName.equalsIgnoreCase(label.trim())
                        || t.name().equalsIgnoreCase(label.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tipo de viaje no soportado: " + label));
    }
}
