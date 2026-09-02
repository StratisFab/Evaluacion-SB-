package page_objects;

import java.util.Arrays;

/** Tipo de viaje: etiqueta en español del feature -> nombre en el sitio y valor del control. */
public enum TripType {
    ROUND_TRIP("Viaje de Ida y Vuelta", "Round Trip", "ROUND_TRIP"),
    ONE_WAY("Solo Ida", "One Way", "ONE_WAY"),
    MULTI_CITY("Multidestino", "Multi-City", "MULTI_STOP");

    private final String spanishLabel;
    private final String accessibleName;
    private final String siteValue;

    TripType(String spanishLabel, String accessibleName, String siteValue) {
        this.spanishLabel = spanishLabel;
        this.accessibleName = accessibleName;
        this.siteValue = siteValue;
    }

    public String accessibleName() {
        return accessibleName;
    }

    public String siteValue() {
        return siteValue;
    }

    public static TripType fromLabel(String label) {
        String normalized = label.trim();
        return Arrays.stream(values())
                .filter(t -> t.spanishLabel.equalsIgnoreCase(normalized)
                        || t.accessibleName.equalsIgnoreCase(normalized)
                        || t.name().equalsIgnoreCase(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tipo de viaje no soportado: " + label));
    }
}
