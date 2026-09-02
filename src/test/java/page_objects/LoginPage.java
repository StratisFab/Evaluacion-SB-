package page_objects;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import java.util.regex.Pattern;

/**
 * Page Object de la pantalla de inicio de sesión a la que redirige el sitio
 * cuando un usuario no autenticado ejecuta una búsqueda de vuelos.
 */
public class LoginPage extends BasePage {

    /** Texto del encabezado en el sitio (inglés). El escenario lo expresa en español. */
    public static final String LOGIN_HEADING_EN = "Log in to my account";

    private final Locator loginHeading;

    public LoginPage(Page page) {
        super(page);
        loginHeading = page.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName(Pattern.compile(LOGIN_HEADING_EN, Pattern.CASE_INSENSITIVE)));
    }

    public void waitForLoginHeading() {
        loginHeading.first().waitFor();
    }

    public boolean isLoginHeadingVisible() {
        return loginHeading.first().isVisible();
    }

    public String getLoginHeadingText() {
        return loginHeading.first().textContent().trim();
    }
}
