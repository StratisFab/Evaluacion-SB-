package page_objects;

import java.util.Arrays;

/**
 * Clases de cabina del buscador de vuelos.
 * Relaciona la etiqueta en español de los escenarios con el nombre accesible del sitio.
 */
public enum CabinClass {
    ECONOMY("Económica", "Economy"),
    PREMIUM_ECONOMY("Económica Premium", "Premium Economy"),
    BUSINESS("Clase Ejecutiva", "Business Class"),
    FIRST("Primera Clase", "First Class");

    private final String spanishLabel;
    private final String accessibleName;

    CabinClass(String spanishLabel, String accessibleName) {
        this.spanishLabel = spanishLabel;
        this.accessibleName = accessibleName;
    }

    public String accessibleName() {
        return accessibleName;
    }

    public static CabinClass fromLabel(String label) {
        return Arrays.stream(values())
                .filter(c -> c.spanishLabel.equalsIgnoreCase(label.trim())
                        || c.accessibleName.equalsIgnoreCase(label.trim())
                        || c.name().equalsIgnoreCase(label.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Clase no soportada: " + label));
    }
}
