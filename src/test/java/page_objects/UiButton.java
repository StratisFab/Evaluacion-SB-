package page_objects;

import java.util.Arrays;

/** Botones nombrados en español en el feature -> texto real en el sitio. */
public enum UiButton {
    HECHO("Hecho", "Done"),
    BUSQUEDA("Búsqueda", "Search");

    private final String spanishLabel;
    private final String siteText;

    UiButton(String spanishLabel, String siteText) {
        this.spanishLabel = spanishLabel;
        this.siteText = siteText;
    }

    public String siteText() {
        return siteText;
    }

    public static UiButton fromLabel(String label) {
        String normalized = label.trim();
        return Arrays.stream(values())
                .filter(b -> b.spanishLabel.equalsIgnoreCase(normalized)
                        || b.siteText.equalsIgnoreCase(normalized)
                        || b.name().equalsIgnoreCase(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Botón no soportado: " + label));
    }
}
