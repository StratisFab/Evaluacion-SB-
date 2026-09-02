package page_objects;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;

/**
 * Base de los page objects: guarda la Page y utilidades comunes.
 */
public abstract class BasePage {

    protected final Page page;

    protected BasePage(Page page) {
        this.page = page;
    }

    public void navigateTo(String url) {
        page.navigate(url);
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
    }

    public String getTitle() {
        return page.title();
    }

    public String getUrl() {
        return page.url();
    }

    // Amex deshabilita sus controles con aria-disabled, no con el atributo disabled
    protected boolean isDisabled(Locator locator) {
        return locator.isDisabled() || "true".equals(locator.getAttribute("aria-disabled"));
    }

    protected boolean isExpanded(Locator locator) {
        return "true".equals(locator.getAttribute("aria-expanded"));
    }
}
