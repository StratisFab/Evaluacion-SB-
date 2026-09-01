package steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import pages.AccountLoginPage;
import pages.FlightsSearchPage;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class BusquedaVuelosSteps {
    private FlightsSearchPage flightsSearchPage;
    private AccountLoginPage accountLoginPage;

    @When("el usuario ingresa {string} en origen y selecciona la primera opción")
    public void ingresaOrigen(String texto) {
        flights().selectOriginFirstOption(texto);
    }

    @When("el usuario ingresa {string} en destino y selecciona la primera opción")
    public void ingresaDestino(String texto) {
        flights().selectDestinationFirstOption(texto);
    }

    @When("el usuario selecciona una fecha de salida posterior a hoy")
    public void seleccionaFechaSalida() {
        flights().selectDepartureAfterToday();
    }

    @When("el usuario selecciona una fecha de regreso posterior a la salida")
    public void seleccionaFechaRegreso() {
        flights().selectReturnAfterDeparture();
    }

    @When("el usuario confirma las fechas")
    public void confirmaFechas() {
        flights().confirmDates();
    }

    @When("el usuario selecciona búsqueda")
    public void seleccionaBusqueda() {
        flights().searchFlights();
    }

    @Then("se muestra el título {string}")
    public void validaTituloLogin(String titulo) {
        if (accountLoginPage == null) {
            accountLoginPage = new AccountLoginPage();
        }
        assertThat(accountLoginPage.loginTitle(titulo)).isVisible();
    }

    private FlightsSearchPage flights() {
        if (flightsSearchPage == null) {
            flightsSearchPage = new FlightsSearchPage();
        }
        return flightsSearchPage;
    }
}
