package page_objects;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;

/**
 * Clase base de todos los Page Objects. Concentra el acceso a {@link Page}
 * y utilidades comunes para no repetirlas en cada página.
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

    /**
     * Un elemento se considera deshabilitado si tiene el atributo nativo {@code disabled}
     * o el atributo de accesibilidad {@code aria-disabled="true"} (patrón usado por Amex).
     */
    protected boolean isDisabled(Locator locator) {
        return locator.isDisabled() || "true".equals(locator.getAttribute("aria-disabled"));
    }

    protected boolean isExpanded(Locator locator) {
        return "true".equals(locator.getAttribute("aria-expanded"));
    }
}
