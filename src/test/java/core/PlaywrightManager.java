package core;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;

import java.nio.file.Path;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;

/**
 * Ciclo de vida de Playwright. El navegador se lanza una vez por hilo y se reutiliza;
 * cada escenario recibe un BrowserContext y una Page nuevos (aislamiento de cookies/storage).
 * ThreadLocal permite correr escenarios en paralelo.
 */
public final class PlaywrightManager {

    public enum TraceMode { OFF, ON_FAILURE, ALWAYS }

    private static final ThreadLocal<Playwright> PLAYWRIGHT = new ThreadLocal<>();
    private static final ThreadLocal<Browser> BROWSER = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> CONTEXT = new ThreadLocal<>();
    private static final ThreadLocal<Page> PAGE = new ThreadLocal<>();

    // Navegadores y drivers de todos los hilos, para cerrarlos al final de la ejecución
    private static final Queue<AutoCloseable> SHARED_RESOURCES = new ConcurrentLinkedQueue<>();

    private static final Pattern MEDIA_RESOURCES =
            Pattern.compile(".*\\.(png|jpe?g|gif|webp|svg|ico|woff2?|ttf|mp4|webm)(\\?.*)?$", Pattern.CASE_INSENSITIVE);

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(PlaywrightManager::shutdown));
    }

    private PlaywrightManager() {
    }

    public static Page startScenario() {
        Browser browser = browserForCurrentThread();

        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                .setViewportSize(
                        ConfigReader.getInt("viewportWidth", 1366),
                        ConfigReader.getInt("viewportHeight", 768));

        // Akamai rechaza el user agent "HeadlessChrome" (Access Denied); en headless se usa uno normal
        String userAgent = ConfigReader.get("userAgent", "");
        if (userAgent.isBlank() && isHeadless()) {
            userAgent = regularUserAgent(browser);
        }
        if (!userAgent.isBlank()) {
            contextOptions.setUserAgent(userAgent);
        }

        BrowserContext context = browser.newContext(contextOptions);
        context.setDefaultTimeout(ConfigReader.getInt("timeout", 30_000));
        context.setDefaultNavigationTimeout(ConfigReader.getInt("navigationTimeout", 60_000));

        if (ConfigReader.getBoolean("blockMedia", false)) {
            context.route(MEDIA_RESOURCES, route -> route.abort());
        }

        if (traceMode() != TraceMode.OFF) {
            context.tracing().start(new Tracing.StartOptions()
                    .setScreenshots(true)
                    .setSnapshots(true)
                    .setSources(true));
        }

        Page page = context.newPage();
        CONTEXT.set(context);
        PAGE.set(page);
        return page;
    }

    public static Page getPage() {
        Page page = PAGE.get();
        if (page == null) {
            throw new IllegalStateException("Playwright no ha sido iniciado. Invoque PlaywrightManager.startScenario() primero.");
        }
        return page;
    }

    /** Cierra el contexto del escenario y guarda el trace en tracePath cuando aplica. */
    public static void endScenario(boolean failed, Path tracePath) {
        BrowserContext context = CONTEXT.get();
        if (context != null) {
            stopTracing(context, failed, tracePath);
            closeQuietly(context);
        }
        PAGE.remove();
        CONTEXT.remove();
    }

    public static synchronized void shutdown() {
        AutoCloseable resource;
        while ((resource = SHARED_RESOURCES.poll()) != null) {
            closeQuietly(resource);
        }
    }

    public static TraceMode traceMode() {
        String value = ConfigReader.get("traceMode", "off").trim().toUpperCase().replace('-', '_');
        return TraceMode.valueOf(value);
    }

    private static Browser browserForCurrentThread() {
        Browser browser = BROWSER.get();
        if (browser != null && browser.isConnected()) {
            return browser;
        }

        Playwright playwright = Playwright.create();
        PLAYWRIGHT.set(playwright);

        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                .setHeadless(isHeadless())
                .setSlowMo(ConfigReader.getInt("slowMo", 0));

        String channel = ConfigReader.get("channel", "");
        if (!channel.isBlank()) {
            launchOptions.setChannel(channel); // "chrome" o "msedge" para usar el navegador instalado
        }

        browser = resolveBrowserType(playwright).launch(launchOptions);
        BROWSER.set(browser);

        SHARED_RESOURCES.add(browser);
        SHARED_RESOURCES.add(playwright);
        return browser;
    }

    // En CI (variable CI definida) se fuerza headless salvo que se pase -Dheadless
    private static boolean isHeadless() {
        boolean runningOnCi = System.getenv("CI") != null && System.getProperty("headless") == null;
        return runningOnCi || ConfigReader.getBoolean("headless", true);
    }

    // Mismo UA que manda Chromium con ventana, sin la marca "Headless"
    private static String regularUserAgent(Browser browser) {
        String major = browser.version().split("\\.")[0];
        String os = System.getProperty("os.name", "").toLowerCase();
        String platform = os.contains("win") ? "Windows NT 10.0; Win64; x64"
                : os.contains("mac") ? "Macintosh; Intel Mac OS X 10_15_7"
                : "X11; Linux x86_64";
        return "Mozilla/5.0 (" + platform + ") AppleWebKit/537.36 (KHTML, like Gecko) Chrome/"
                + major + ".0.0.0 Safari/537.36";
    }

    private static void stopTracing(BrowserContext context, boolean failed, Path tracePath) {
        TraceMode mode = traceMode();
        if (mode == TraceMode.OFF) {
            return;
        }
        boolean keepTrace = mode == TraceMode.ALWAYS || failed;
        try {
            if (keepTrace && tracePath != null) {
                context.tracing().stop(new Tracing.StopOptions().setPath(tracePath));
            } else {
                context.tracing().stop();
            }
        } catch (Exception ignored) {
            // el trace es solo diagnóstico, no debe romper la limpieza
        }
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
            // nada que hacer
        }
    }
}
