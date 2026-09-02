package steps_definitions;

import com.microsoft.playwright.Page;
import core.ConfigReader;
import core.PlaywrightManager;
import core.TestContext;
import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Before: contexto y página nuevos por escenario. After: evidencias si falló y cierre del contexto.
 * AfterAll: cierra navegadores y drivers.
 */
public class Hooks {

    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final TestContext testContext;

    public Hooks(TestContext testContext) {
        this.testContext = testContext;
    }

    @Before
    public void setUp() {
        testContext.setPage(PlaywrightManager.startScenario());
    }

    @After
    public void tearDown(Scenario scenario) {
        String evidenceName = evidenceName(scenario);
        try {
            if (scenario.isFailed()) {
                attachFailureEvidence(scenario, evidenceName);
            }
        } finally {
            Path tracePath = Paths.get(ConfigReader.get("tracesDir", "build/traces"), evidenceName + ".zip");
            PlaywrightManager.endScenario(scenario.isFailed(), tracePath);
        }
    }

    @AfterAll
    public static void shutdown() {
        PlaywrightManager.shutdown();
    }

    private void attachFailureEvidence(Scenario scenario, String evidenceName) {
        try {
            Page page = testContext.getPage();
            scenario.log("URL al momento del fallo: " + page.url());
            Path path = Paths.get(ConfigReader.get("screenshotsDir", "build/screenshots"), evidenceName + ".png");
            byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setPath(path));
            scenario.attach(screenshot, "image/png", scenario.getName());
        } catch (Exception e) {
            scenario.log("No fue posible capturar la evidencia: " + e.getMessage());
        }
    }

    // nombre único por escenario y corrida (línea del feature + timestamp)
    private static String evidenceName(Scenario scenario) {
        String safeName = scenario.getName().replaceAll("[^\\p{Alnum}-_]", "_");
        return safeName + "_L" + scenario.getLine() + "_" + LocalDateTime.now().format(TIMESTAMP);
    }
}
