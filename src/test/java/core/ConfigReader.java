package core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Lee configuration/configuration.properties. Prioridad: -Dclave=valor > archivo > default.
 */
public final class ConfigReader {

    private static final String CONFIG_FILE = "configuration/configuration.properties";
    private static final Properties PROPERTIES = load();

    private ConfigReader() {
    }

    private static Properties load() {
        Properties props = new Properties();
        try (InputStream in = ConfigReader.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (in == null) {
                throw new IllegalStateException("No se encontró " + CONFIG_FILE + " en el classpath");
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("No fue posible leer " + CONFIG_FILE, e);
        }
        return props;
    }

    public static String get(String key, String defaultValue) {
        String systemValue = System.getProperty(key);
        if (systemValue != null && !systemValue.isBlank()) {
            return systemValue;
        }
        return PROPERTIES.getProperty(key, defaultValue);
    }

    public static String get(String key) {
        String value = get(key, null);
        if (value == null) {
            throw new IllegalStateException("Propiedad de configuración no definida: " + key);
        }
        return value;
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return Boolean.parseBoolean(get(key, String.valueOf(defaultValue)));
    }

    public static int getInt(String key, int defaultValue) {
        return Integer.parseInt(get(key, String.valueOf(defaultValue)).trim());
    }

    public static String baseUrl() {
        return get("baseUrl");
    }
}
