package page_objects;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import java.util.regex.Pattern;

/**
 * Pantalla de login a la que redirige el sitio al buscar sin sesión iniciada.
 */
public class LoginPage extends BasePage {

    // El sitio está en inglés; el feature lo expresa en español ("Iniciar sesión en mi cuenta").
    public static final String LOGIN_HEADING_EN = "Log in to my account";

    private final Locator loginHeading;

    public LoginPage(Page page) {
        super(page);
        loginHeading = page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions()
                .setName(Pattern.compile(LOGIN_HEADING_EN, Pattern.CASE_INSENSITIVE))).first();
    }

    public void waitForLoginHeading() {
        loginHeading.waitFor();
    }

    public boolean isLoginHeadingVisible() {
        return loginHeading.isVisible();
    }

    public String getLoginHeadingText() {
        return loginHeading.textContent().trim();
    }
}
