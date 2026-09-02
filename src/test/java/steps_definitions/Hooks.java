package steps_definitions;

import com.microsoft.playwright.Page;
import core.ConfigReader;
import core.PlaywrightManager;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Hooks de Cucumber: levantan y cierran el navegador para cada escenario,
 * de modo que cada uno sea independiente. Si un escenario falla, adjunta una
 * captura de pantalla al reporte y la guarda en disco.
 */
public class Hooks {

    @Before
    public void setUp() {
        PlaywrightManager.start();
    }

    @After
    public void tearDown(Scenario scenario) {
        try {
            if (scenario.isFailed()) {
                captureScreenshot(scenario);
            }
        } finally {
            PlaywrightManager.stop();
        }
    }

    private void captureScreenshot(Scenario scenario) {
        try {
            Page page = PlaywrightManager.getPage();
            String fileName = scenario.getName().replaceAll("[^a-zA-Z0-9-_]", "_") + ".png";
            Path path = Paths.get(ConfigReader.get("screenshotsDir", "build/screenshots"), fileName);
            byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setPath(path).setFullPage(true));
            scenario.attach(screenshot, "image/png", scenario.getName());
        } catch (Exception e) {
            scenario.log("No fue posible capturar la pantalla: " + e.getMessage());
        }
    }
}
