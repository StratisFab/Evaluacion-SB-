package fixtures;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import config.ConfigReader;

import java.util.Map;

public final class PlaywrightManager {
    private static final ThreadLocal<Playwright> PLAYWRIGHT = new ThreadLocal<>();
    private static final ThreadLocal<Browser> BROWSER = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> CONTEXT = new ThreadLocal<>();
    private static final ThreadLocal<Page> PAGE = new ThreadLocal<>();

    private PlaywrightManager() {
    }

    public static void start() {
        Playwright playwright = Playwright.create();
        PLAYWRIGHT.set(playwright);

        Browser browser = resolveBrowser(playwright).launch(
                new BrowserType.LaunchOptions().setHeadless(ConfigReader.getBoolean("browser.headless"))
        );
        BROWSER.set(browser);

        BrowserContext context = browser.newContext(
                new Browser.NewContextOptions()
                        .setBaseURL(ConfigReader.get("app.baseUrl"))
                        .setViewportSize(1440, 900)
                        .setLocale("en-US")
                        .setTimezoneId("America/Mexico_City")
                        .setExtraHTTPHeaders(Map.of("Accept-Language", "es-MX,es;q=0.9,en-US;q=0.8"))
                        .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36")
        );
        context.setDefaultTimeout(ConfigReader.getInt("browser.timeout"));
        context.setDefaultNavigationTimeout(ConfigReader.getInt("browser.navigation.timeout"));
        CONTEXT.set(context);
        PAGE.set(context.newPage());
    }

    public static Page getPage() {
        Page page = PAGE.get();
        if (page == null) {
            throw new IllegalStateException("Playwright no está iniciado. Revisa el Hook.");
        }
        return page;
    }

    public static void stop() {
        closeQuietly(PAGE.get());
        closeQuietly(CONTEXT.get());
        closeQuietly(BROWSER.get());
        closeQuietly(PLAYWRIGHT.get());
        PAGE.remove();
        CONTEXT.remove();
        BROWSER.remove();
        PLAYWRIGHT.remove();
    }

    private static BrowserType resolveBrowser(Playwright playwright) {
        return switch (ConfigReader.get("browser.name").toLowerCase()) {
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
            // El After no debe ocultar el resultado del escenario
        }
    }
}
