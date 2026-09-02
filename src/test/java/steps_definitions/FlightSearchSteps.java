package steps_definitions;

import core.ConfigReader;
import core.TestContext;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import io.cucumber.java.es.Y;
import page_objects.CabinClass;
import page_objects.FlightsSearchPage;
import page_objects.LoginPage;
import page_objects.TripType;
import page_objects.UiButton;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Steps del feature de búsqueda de vuelos. Llaman a los page objects y hacen las aserciones;
 * aquí no hay localizadores.
 */
public class FlightSearchSteps {

    private static final Pattern IATA_CODE = Pattern.compile("\\(([A-Za-z]{3})\\)");

    private final TestContext context;
    private final FlightsSearchPage flightsSearchPage;
    private final LoginPage loginPage;

    public FlightSearchSteps(TestContext context) {
        this.context = context;
        this.flightsSearchPage = context.flightsSearchPage();
        this.loginPage = context.loginPage();
    }

    // ---------------- Navegación

    @Dado("que el usuario ingresa a la página de vuelos de American Express Travel")
    public void elUsuarioIngresaALaPaginaDeVuelos() {
        flightsSearchPage.open(ConfigReader.baseUrl());
    }

    // ---------------- Tipo de viaje / clase

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

    // ---------------- Viajeros

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

    @Entonces("el botón para disminuir adultos debe estar habilitado")
    public void elBotonParaDisminuirAdultosDebeEstarHabilitado() {
        assertThat(flightsSearchPage.isDecreaseAdultsDisabled())
                .as("El botón (-) de adultos debería estar habilitado")
                .isFalse();
    }

    @Cuando("confirma la selección de viajeros con {string}")
    public void confirmaLaSeleccionDeViajeros(String boton) {
        UiButton button = UiButton.fromLabel(boton);
        assertThat(flightsSearchPage.getTravelersDoneButtonText())
                .as("El botón de confirmación de viajeros debería ser '%s'", boton)
                .isEqualToIgnoringCase(button.siteText());
        flightsSearchPage.confirmTravelers();
    }

    @Entonces("el resumen de viajeros debe indicar {int} viajero(s)")
    public void elResumenDeViajerosDebeIndicar(int viajeros) {
        assertThat(flightsSearchPage.getTravelersSummary())
                .as("Resumen de viajeros")
                .startsWith(String.valueOf(viajeros));
    }

    // ---------------- Origen / destino

    @Y("ingresa {string} en el origen y selecciona la primera opción {string}")
    public void ingresaElOrigenYSeleccionaLaPrimeraOpcion(String texto, String ciudadEsperada) {
        flightsSearchPage.typeOrigin(texto);
        seleccionarPrimeraSugerencia("origen", ciudadEsperada);
        validarUbicacionCapturada("origen", flightsSearchPage.getOriginValue(), ciudadEsperada);
    }

    @Y("ingresa {string} en el destino y selecciona la primera opción {string}")
    public void ingresaElDestinoYSeleccionaLaPrimeraOpcion(String texto, String ciudadEsperada) {
        flightsSearchPage.typeDestination(texto);
        seleccionarPrimeraSugerencia("destino", ciudadEsperada);
        validarUbicacionCapturada("destino", flightsSearchPage.getDestinationValue(), ciudadEsperada);
    }

    // El sitio devuelve los nombres en inglés ("MEX, Mexico City"), así que se compara con el
    // código IATA que va entre paréntesis en el feature: "Ciudad de México (MEX)".
    private void seleccionarPrimeraSugerencia(String campo, String ciudadEsperada) {
        String codigoIata = extraerCodigoIata(ciudadEsperada);
        boolean coincide = flightsSearchPage.waitForFirstSuggestionContaining(codigoIata);
        assertThat(coincide)
                .as("La primera sugerencia de %s fue '%s' y debería corresponder a '%s'",
                        campo, flightsSearchPage.getFirstSuggestionText(), ciudadEsperada)
                .isTrue();
        flightsSearchPage.selectFirstSuggestion();
    }

    private void validarUbicacionCapturada(String campo, String valorCapturado, String ciudadEsperada) {
        assertThat(valorCapturado)
                .as("El %s capturado debería contener el código de '%s'", campo, ciudadEsperada)
                .containsIgnoringCase(extraerCodigoIata(ciudadEsperada));
    }

    private static String extraerCodigoIata(String ciudad) {
        Matcher matcher = IATA_CODE.matcher(ciudad);
        if (!matcher.find()) {
            throw new IllegalArgumentException(
                    "Indique el código IATA entre paréntesis, por ejemplo 'Cancún (CUN)': " + ciudad);
        }
        return matcher.group(1).toUpperCase();
    }

    // ---------------- Fechas

    @Y("selecciona una fecha de salida posterior a la fecha actual")
    public void seleccionaUnaFechaDeSalidaPosteriorALaFechaActual() {
        LocalDate departureDate = LocalDate.now().plusDays(ConfigReader.getInt("departureOffsetDays", 10));
        assertThat(departureDate).as("La fecha de salida debe ser posterior a hoy").isAfter(LocalDate.now());
        flightsSearchPage.selectDepartureDate(departureDate);
        context.setDepartureDate(departureDate);
    }

    @Y("selecciona una fecha de regreso posterior a la fecha de salida")
    public void seleccionaUnaFechaDeRegresoPosteriorALaFechaDeSalida() {
        LocalDate departureDate = context.getDepartureDate();
        assertThat(departureDate).as("Primero debe seleccionarse la fecha de salida").isNotNull();
        LocalDate returnDate = departureDate.plusDays(ConfigReader.getInt("tripLengthDays", 5));
        assertThat(returnDate).as("La fecha de regreso debe ser posterior a la salida").isAfter(departureDate);
        flightsSearchPage.selectReturnDate(returnDate);
        context.setReturnDate(returnDate);
    }

    @Y("confirma la selección de fechas con {string}")
    public void confirmaLaSeleccionDeFechas(String boton) {
        UiButton button = UiButton.fromLabel(boton);
        assertThat(flightsSearchPage.getDatesDoneButtonText())
                .as("El botón de confirmación de fechas debería ser '%s'", boton)
                .isEqualToIgnoringCase(button.siteText());
        assertThat(flightsSearchPage.isDatesDoneEnabled())
                .as("El botón '%s' debería habilitarse una vez elegidas ambas fechas", boton)
                .isTrue();
        flightsSearchPage.confirmDates();
        assertThat(flightsSearchPage.getDepartureDateText())
                .as("La fecha de salida debería quedar capturada en el formulario")
                .isNotBlank();
        assertThat(flightsSearchPage.getReturnDateText())
                .as("La fecha de regreso debería quedar capturada en el formulario")
                .isNotBlank();
    }

    // ---------------- Búsqueda / login

    @Y("presiona el botón {string}")
    public void presionaElBoton(String boton) {
        UiButton button = UiButton.fromLabel(boton);
        assertThat(flightsSearchPage.getSearchButtonText())
                .as("El botón de búsqueda debería ser '%s'", boton)
                .isEqualToIgnoringCase(button.siteText());
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
