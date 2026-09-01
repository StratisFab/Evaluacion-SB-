package config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ConfigReader {
    private static final Properties PROPERTIES = new Properties();

    static {
        load("config/config.properties");
        load("data/test-data.properties");
    }

    private ConfigReader() {
    }

    private static void load(String classpathLocation) {
        try (InputStream input = ConfigReader.class.getClassLoader().getResourceAsStream(classpathLocation)) {
            if (input == null) {
                throw new IllegalStateException("No se encontró el archivo de configuración: " + classpathLocation);
            }
            PROPERTIES.load(input);
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudo leer el archivo: " + classpathLocation, exception);
        }
    }

    public static String get(String key) {
        String environmentValue = System.getenv(toEnvKey(key));
        if (isPresent(environmentValue)) {
            return environmentValue.trim();
        }

        String systemValue = System.getProperty(key);
        if (isPresent(systemValue)) {
            return systemValue.trim();
        }

        String value = PROPERTIES.getProperty(key);
        if (!isPresent(value)) {
            throw new IllegalArgumentException("Clave no encontrada o vacía en configuración: " + key);
        }
        return value.trim();
    }

    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }

    public static int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    private static String toEnvKey(String key) {
        return key.toUpperCase().replace('.', '_');
    }

    private static boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }
}
