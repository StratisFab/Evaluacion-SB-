package hooks;

import fixtures.PlaywrightManager;
import io.cucumber.java.After;
import io.cucumber.java.Before;

public class Hook {

    @Before
    public void setUp() {
        PlaywrightManager.start();
    }

    @After
    public void tearDown() {
        PlaywrightManager.stop();
    }
}
