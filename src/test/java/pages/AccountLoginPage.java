package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;

import java.util.regex.Pattern;

public class AccountLoginPage extends BasePage {

    public Locator loginTitle(String expectedTitle) {
        Pattern title = Pattern.compile(
                Pattern.quote(expectedTitle) + "|Log in to my account|Log in to your account",
                Pattern.CASE_INSENSITIVE
        );
        return byRole(AriaRole.HEADING, title)
                .or(page.getByText(title));
    }
}
