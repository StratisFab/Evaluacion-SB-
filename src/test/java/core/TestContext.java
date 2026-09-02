package core;

import com.microsoft.playwright.Page;
import page_objects.FlightsSearchPage;
import page_objects.LoginPage;

import java.time.LocalDate;

/**
 * Estado del escenario en curso. Cucumber (PicoContainer) crea una instancia por escenario
 * y la inyecta en hooks y steps, así se comparten page objects y datos sin estáticos.
 */
public class TestContext {

    private Page page;
    private FlightsSearchPage flightsSearchPage;
    private LoginPage loginPage;

    private LocalDate departureDate;
    private LocalDate returnDate;

    public void setPage(Page page) {
        this.page = page;
        this.flightsSearchPage = null;
        this.loginPage = null;
    }

    public Page getPage() {
        if (page == null) {
            throw new IllegalStateException("La página no ha sido inicializada (¿se ejecutó el hook @Before?)");
        }
        return page;
    }

    public FlightsSearchPage flightsSearchPage() {
        if (flightsSearchPage == null) {
            flightsSearchPage = new FlightsSearchPage(getPage());
        }
        return flightsSearchPage;
    }

    public LoginPage loginPage() {
        if (loginPage == null) {
            loginPage = new LoginPage(getPage());
        }
        return loginPage;
    }

    public LocalDate getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(LocalDate departureDate) {
        this.departureDate = departureDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }
}
