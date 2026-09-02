package core;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

/**
 * Administra el ciclo de vida de Playwright: {@code Playwright -> Browser -> BrowserContext -> Page}.
 * <p>
 * Cada hilo obtiene su propia instancia mediante {@link ThreadLocal}, lo que permite
 * ejecutar escenarios en paralelo de forma segura. Los hooks de Cucumber invocan
 * {@link #start()} antes de cada escenario y {@link #stop()} al terminarlo.
 */
public final class PlaywrightManager {

    private static final ThreadLocal<Playwright> PLAYWRIGHT = new ThreadLocal<>();
    private static final ThreadLocal<Browser> BROWSER = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> CONTEXT = new ThreadLocal<>();
    private static final ThreadLocal<Page> PAGE = new ThreadLocal<>();

    private PlaywrightManager() {
    }

    /** Crea navegador, contexto y página nuevos para el escenario en curso. */
    public static Page start() {
        Playwright playwright = Playwright.create();
        PLAYWRIGHT.set(playwright);

        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                .setHeadless(ConfigReader.getBoolean("headless", true))
                .setSlowMo(ConfigReader.getInt("slowMo", 0));

        Browser browser = resolveBrowserType(playwright).launch(launchOptions);
        BROWSER.set(browser);

        BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                .setViewportSize(
                        ConfigReader.getInt("viewportWidth", 1366),
                        ConfigReader.getInt("viewportHeight", 768)));
        context.setDefaultTimeout(ConfigReader.getInt("timeout", 30_000));
        CONTEXT.set(context);

        Page page = context.newPage();
        PAGE.set(page);
        return page;
    }

    public static Page getPage() {
        Page page = PAGE.get();
        if (page == null) {
            throw new IllegalStateException("Playwright no ha sido iniciado. Invoque PlaywrightManager.start() primero.");
        }
        return page;
    }

    /** Cierra todos los recursos asociados al hilo actual. */
    public static void stop() {
        closeQuietly(CONTEXT.get());
        closeQuietly(BROWSER.get());
        closeQuietly(PLAYWRIGHT.get());
        PAGE.remove();
        CONTEXT.remove();
        BROWSER.remove();
        PLAYWRIGHT.remove();
    }

    private static BrowserType resolveBrowserType(Playwright playwright) {
        String browser = ConfigReader.get("browser", "chromium").toLowerCase();
        return switch (browser) {
            case "firefox" -> playwright.firefox();
            case "webkit" -> playwright.webkit();
            default -> playwright.chromium();
        };
    }

    private static void closeQuietly(AutoCloseable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception ignored) {
            // No interesa fallar el escenario por un error al liberar recursos.
        }
    }
}
