package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitUntilState;
import fixtures.PlaywrightManager;

import java.util.regex.Pattern;

public abstract class BasePage {
    protected final Page page;

    protected BasePage() {
        this.page = PlaywrightManager.getPage();
    }

    protected void navigate(String path) {
        page.navigate(path, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
    }

    protected Locator byRole(AriaRole role, Pattern name) {
        return page.getByRole(role, new Page.GetByRoleOptions().setName(name));
    }

    protected Locator firstOption() {
        return page.getByRole(AriaRole.OPTION).first();
    }

    protected void typeInto(Locator locator, String text) {
        locator.click();
        locator.fill("");
        locator.pressSequentially(text, new Locator.PressSequentiallyOptions().setDelay(80));
    }

    protected void click(Locator locator) {
        locator.click();
    }

    protected void clickIfVisible(Locator locator) {
        if (locator.count() > 0 && locator.first().isVisible()) {
            locator.first().click();
        }
    }
}
