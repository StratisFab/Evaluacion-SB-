package steps_definitions;

import core.ConfigReader;
import core.PlaywrightManager;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import io.cucumber.java.es.Y;
import page_objects.CabinClass;
import page_objects.FlightsSearchPage;
import page_objects.LoginPage;
import page_objects.TripType;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Step definitions del feature de búsqueda de vuelos.
 * Solo orquestan llamadas a los Page Objects y contienen las aserciones;
 * nunca manipulan localizadores directamente.
 */
public class FlightSearchSteps {

    private final FlightsSearchPage flightsSearchPage;
    private final LoginPage loginPage;

    private LocalDate departureDate;
    private LocalDate returnDate;

    public FlightSearchSteps() {
        this.flightsSearchPage = new FlightsSearchPage(PlaywrightManager.getPage());
        this.loginPage = new LoginPage(PlaywrightManager.getPage());
    }

    // ---------------------------------------------------------------- Navegación

    @Dado("que el usuario ingresa a la página de vuelos de American Express Travel")
    public void elUsuarioIngresaALaPaginaDeVuelos() {
        flightsSearchPage.open(ConfigReader.baseUrl());
    }

    // ---------------------------------------------------------------- Tipo de viaje / clase

    @Cuando("selecciona el tipo de viaje {string}")
    public void seleccionaElTipoDeViaje(String tipoDeViaje) {
        TripType tripType = TripType.fromLabel(tipoDeViaje);
        flightsSearchPage.selectTripType(tripType);
        assertThat(flightsSearchPage.isTripTypeSelected(tripType))
                .as("El tipo de viaje '%s' debería quedar seleccionado", tipoDeViaje)
                .isTrue();
    }

    @Y("selecciona la clase {string}")
    public void seleccionaLaClase(String clase) {
        CabinClass cabinClass = CabinClass.fromLabel(clase);
        flightsSearchPage.selectCabinClass(cabinClass);
        assertThat(flightsSearchPage.getSelectedCabinClass())
                .as("La clase seleccionada debería ser '%s'", clase)
                .isEqualToIgnoringCase(cabinClass.accessibleName());
    }

    // ---------------------------------------------------------------- Viajeros

    @Y("selecciona {int} adulto(s) como viajero(s)")
    public void seleccionaAdultosComoViajeros(int adultos) {
        flightsSearchPage.setAdults(adultos);
        assertThat(flightsSearchPage.getAdultsCount())
                .as("El número de adultos seleccionados")
                .isEqualTo(adultos);
    }

    @Entonces("el botón para disminuir adultos debe estar deshabilitado")
    public void elBotonParaDisminuirAdultosDebeEstarDeshabilitado() {
        assertThat(flightsSearchPage.isDecreaseAdultsDisabled())
                .as("El botón (-) de adultos debería estar deshabilitado")
                .isTrue();
    }

    @Cuando("confirma la selección de viajeros con {string}")
    public void confirmaLaSeleccionDeViajeros(String boton) {
        flightsSearchPage.confirmTravelers();
    }

    // ---------------------------------------------------------------- Origen / destino

    @Y("ingresa {string} en el origen y selecciona la primera opción {string}")
    public void ingresaElOrigenYSeleccionaLaPrimeraOpcion(String texto, String ciudadEsperada) {
        flightsSearchPage.typeOrigin(texto);
        String primeraOpcion = flightsSearchPage.getFirstSuggestionText();
        flightsSearchPage.selectFirstSuggestion();
        validarUbicacionSeleccionada("origen", flightsSearchPage.getOriginValue(), primeraOpcion, ciudadEsperada);
    }

    @Y("ingresa {string} en el destino y selecciona la primera opción {string}")
    public void ingresaElDestinoYSeleccionaLaPrimeraOpcion(String texto, String ciudadEsperada) {
        flightsSearchPage.typeDestination(texto);
        String primeraOpcion = flightsSearchPage.getFirstSuggestionText();
        flightsSearchPage.selectFirstSuggestion();
        validarUbicacionSeleccionada("destino", flightsSearchPage.getDestinationValue(), primeraOpcion, ciudadEsperada);
    }


    /**
     * Valida que el valor capturado en el campo corresponda a la primera sugerencia y a la
     * ciudad esperada. El sitio muestra los nombres en inglés ("MEX, Mexico City"), por lo que
     * la comparación se hace con el código IATA indicado entre paréntesis en el escenario,
     * por ejemplo "Ciudad de México (MEX)".
     */
    private void validarUbicacionSeleccionada(String campo, String valorCapturado, String primeraOpcion, String ciudadEsperada) {
        String codigoIata = extraerCodigoIata(ciudadEsperada);
        assertThat(valorCapturado)
                .as("El %s debería contener el código '%s' de '%s'", campo, codigoIata, ciudadEsperada)
                .containsIgnoringCase(codigoIata);
        assertThat(primeraOpcion)
                .as("La primera sugerencia debería corresponder al %s seleccionado", campo)
                .containsIgnoringCase(codigoIata);
    }

    private static String extraerCodigoIata(String ciudad) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\(([A-Za-z]{3})\\)").matcher(ciudad);
        if (!m.find()) {
            throw new IllegalArgumentException("Indique el código IATA entre paréntesis, por ejemplo 'Cancún (CUN)': " + ciudad);
        }
        return m.group(1).toUpperCase();
    }

    // ---------------------------------------------------------------- Fechas

    @Y("selecciona una fecha de salida posterior a la fecha actual")
    public void seleccionaUnaFechaDeSalidaPosteriorALaFechaActual() {
        departureDate = LocalDate.now().plusDays(ConfigReader.getInt("departureOffsetDays", 10));
        flightsSearchPage.selectDepartureDate(departureDate);
        assertThat(departureDate).isAfter(LocalDate.now());
    }

    @Y("selecciona una fecha de regreso posterior a la fecha de salida")
    public void seleccionaUnaFechaDeRegresoPosteriorALaFechaDeSalida() {
        returnDate = departureDate.plusDays(ConfigReader.getInt("tripLengthDays", 5));
        flightsSearchPage.selectReturnDate(returnDate);
        assertThat(returnDate).isAfter(departureDate);
    }

    @Y("confirma la selección de fechas con {string}")
    public void confirmaLaSeleccionDeFechas(String boton) {
        flightsSearchPage.confirmDates();
        assertThat(flightsSearchPage.getDepartureDateText())
                .as("La fecha de salida debería estar capturada")
                .isNotBlank();
        assertThat(flightsSearchPage.getReturnDateText())
                .as("La fecha de regreso debería estar capturada")
                .isNotBlank();
    }

    // ---------------------------------------------------------------- Búsqueda / login

    @Y("presiona el botón {string}")
    public void presionaElBoton(String boton) {
        flightsSearchPage.clickSearch();
    }

    @Entonces("se debe mostrar el título {string}")
    public void seDebeMostrarElTitulo(String tituloEsperado) {
        loginPage.waitForLoginHeading();
        assertThat(loginPage.isLoginHeadingVisible())
                .as("Debería mostrarse el título '%s' (en el sitio: '%s')", tituloEsperado, LoginPage.LOGIN_HEADING_EN)
                .isTrue();
        assertThat(loginPage.getLoginHeadingText())
                .as("Texto del encabezado de inicio de sesión")
                .isEqualToIgnoringCase(LoginPage.LOGIN_HEADING_EN);
    }
}
