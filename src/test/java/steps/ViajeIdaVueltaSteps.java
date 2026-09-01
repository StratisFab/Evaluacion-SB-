package steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import pages.FlightsSearchPage;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class ViajeIdaVueltaSteps {
    private FlightsSearchPage flightsSearchPage;

    @Given("el usuario está en la página de vuelos de American Express Travel")
    public void abrirPaginaDeVuelos() {
        flightsSearchPage = new FlightsSearchPage();
        flightsSearchPage.open();
    }

    @When("el usuario selecciona viaje de ida y vuelta")
    public void seleccionaIdaYVuelta() {
        flightsSearchPage.selectRoundTrip();
    }

    @When("el usuario selecciona primera clase")
    public void seleccionaPrimeraClase() {
        flightsSearchPage.selectFirstClass();
    }

    @When("el usuario selecciona un adulto")
    public void seleccionaUnAdulto() {
        flightsSearchPage.openTravelers();
    }

    @Then("el botón de disminuir adultos está deshabilitado")
    public void botonDisminuirDeshabilitado() {
        assertThat(flightsSearchPage.adultDecreaseButton()).isDisabled();
    }

    @When("el usuario confirma los viajeros")
    public void confirmaViajeros() {
        flightsSearchPage.confirmTravelers();
    }
}
