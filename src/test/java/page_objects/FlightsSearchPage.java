package page_objects;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * Buscador de vuelos de Amex Travel (/en-us/travel/flights).
 * Localizadores por id, data-testid y roles ARIA; nada de xpath posicional.
 * Sin aserciones: solo acciones y consultas de estado, las validaciones van en los steps.
 */
public class FlightsSearchPage extends BasePage {

    private static final int MAX_MONTHS_TO_NAVIGATE = 12;
    private static final int MAX_OPEN_ATTEMPTS = 3;
    private static final double SHORT_WAIT_MS = 5_000;

    // Tipo de viaje: radios en escritorio, dropdown en pantallas chicas
    private final Locator tripTypeControl;
    private final Locator tripTypeDropdown;

    // Clase
    private final Locator cabinClassDropdown;

    // Viajeros
    private final Locator travelersButton;
    private final Locator adultsRow;
    private final Locator adultsInput;
    private final Locator decreaseAdultsButton;
    private final Locator increaseAdultsButton;
    private final Locator travelersDoneButton;

    // Origen / destino
    private final Locator originInput;
    private final Locator destinationInput;
    private final Locator suggestionsListbox;
    private final Locator suggestionOptions;

    // Fechas
    private final Locator departureDateButton;
    private final Locator returnDateButton;
    private final Locator datePopup;
    private final Locator calendar;
    private final Locator nextMonthButton;
    private final Locator datesDoneButton;

    // Búsqueda
    private final Locator searchButton;

    public FlightsSearchPage(Page page) {
        super(page);

        tripTypeControl = page.getByTestId("trip-type-segmented-control");
        tripTypeDropdown = page.locator("#trip-type-dropdown");

        cabinClassDropdown = page.locator("#flight-class-dropdown");

        travelersButton = page.locator("#axp-travel-search-rooms-travelers-flights_popupButton");
        adultsRow = page.getByTestId("adult-stepper-row");
        adultsInput = adultsRow.getByRole(AriaRole.SPINBUTTON);
        decreaseAdultsButton = adultsRow.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName(Pattern.compile("^Decrease .*Adult", Pattern.CASE_INSENSITIVE)));
        increaseAdultsButton = adultsRow.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName(Pattern.compile("^Increase .*Adult", Pattern.CASE_INSENSITIVE)));
        travelersDoneButton = page.locator("#axp-travel-search-rooms-travelers-flights_doneButton");

        originInput = page.locator("#axp-travel-search-locations_locationsInput_departure");
        destinationInput = page.locator("#axp-travel-search-locations_locationsInput_destination");
        suggestionsListbox = page.locator("#suggestionsListbox");
        suggestionOptions = suggestionsListbox.getByRole(AriaRole.OPTION);

        departureDateButton = page.locator("#date-picker-popup-button-start-date");
        returnDateButton = page.locator("#date-picker-popup-button-end-date");
        // Ojo: el calendario NO cuelga de #date_popup sino del contenedor date-picker-popup,
        // y viene duplicado (escritorio/móvil), por eso el filtro visible=true.
        datePopup = page.getByTestId("date-picker-popup");
        calendar = datePopup.getByTestId("date-picker-calendar").locator("visible=true");
        nextMonthButton = datePopup.locator("#nextMonthBtn").locator("visible=true");
        datesDoneButton = datePopup.locator("#doneBtn").locator("visible=true");

        searchButton = page.locator("#axp-travel-search-flights_searchButton");
    }

    // ---------------- Navegación

    public void open(String url) {
        navigateTo(url);
        if (getTitle().contains("Access Denied")) {
            throw new IllegalStateException("El sitio bloqueó el acceso (Akamai). Revise user agent / red: " + url);
        }
        searchButton.waitFor();
    }

    // ---------------- Tipo de viaje y clase

    // En escritorio es un grupo de radios; en pantallas chicas el sitio lo cambia por un dropdown.
    public void selectTripType(TripType tripType) {
        if (tripTypeControl.isVisible()) {
            tripTypeRadio(tripType).click();
        } else {
            tripTypeDropdown.click();
            page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions()
                    .setName(tripType.accessibleName()).setExact(true)).click();
        }
    }

    public boolean isTripTypeSelected(TripType tripType) {
        if (tripTypeControl.isVisible()) {
            return "true".equals(tripTypeRadio(tripType).getAttribute("aria-checked"));
        }
        return tripType.siteValue().equals(tripTypeDropdown.getAttribute("value"));
    }

    private Locator tripTypeRadio(TripType tripType) {
        return tripTypeControl.getByRole(AriaRole.RADIO,
                new Locator.GetByRoleOptions().setName(tripType.accessibleName()).setExact(true));
    }

    public void selectCabinClass(CabinClass cabinClass) {
        cabinClassDropdown.click();
        page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions()
                .setName(cabinClass.accessibleName()).setExact(true)).click();
    }

    public String getSelectedCabinClass() {
        return cabinClassDropdown.textContent().trim();
    }

    // ---------------- Viajeros

    public void openTravelersPanel() {
        if (!isExpanded(travelersButton)) {
            travelersButton.click();
        }
        adultsRow.waitFor();
    }

    // Ajusta adultos con +/- hasta la cantidad pedida. Se valida contra min/max del control
    // para no quedarse esperando un botón deshabilitado hasta el timeout.
    public void setAdults(int desired) {
        openTravelersPanel();
        int min = attributeAsInt(adultsInput, "min", 1);
        int max = attributeAsInt(adultsInput, "max", 9);
        if (desired < min || desired > max) {
            throw new IllegalArgumentException(
                    "El número de adultos debe estar entre %d y %d, se recibió %d".formatted(min, max, desired));
        }
        int current = getAdultsCount();
        while (current < desired) {
            increaseAdultsButton.click();
            current++;
        }
        while (current > desired) {
            decreaseAdultsButton.click();
            current--;
        }
    }

    public int getAdultsCount() {
        return Integer.parseInt(adultsInput.inputValue().trim());
    }

    public boolean isDecreaseAdultsDisabled() {
        return isDisabled(decreaseAdultsButton);
    }

    public String getTravelersDoneButtonText() {
        return travelersDoneButton.textContent().trim();
    }

    public String getTravelersSummary() {
        return travelersButton.textContent().trim();
    }

    public void confirmTravelers() {
        travelersDoneButton.click();
        adultsRow.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
    }

    // ---------------- Origen y destino

    public void typeOrigin(String text) {
        typeLocation(originInput, text);
    }

    public void typeDestination(String text) {
        typeLocation(destinationInput, text);
    }

    private void typeLocation(Locator input, String text) {
        input.click();
        input.fill(text);
        suggestionOptions.first().waitFor();
    }

    // Espera a que la primera sugerencia contenga el texto esperado (p. ej. el código IATA).
    // Evita tomar resultados de la consulta anterior mientras llega la respuesta del servidor.
    public boolean waitForFirstSuggestionContaining(String expectedText) {
        String expected = expectedText.toUpperCase();
        try {
            page.waitForCondition(() -> getFirstSuggestionText().toUpperCase().contains(expected),
                    new Page.WaitForConditionOptions().setTimeout(SHORT_WAIT_MS));
            return true;
        } catch (TimeoutError e) {
            return false;
        }
    }

    // Selecciona la primera sugerencia y espera a que se cierre la lista
    public void selectFirstSuggestion() {
        suggestionOptions.first().click();
        suggestionsListbox.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
    }

    public String getFirstSuggestionText() {
        return suggestionOptions.first().textContent().trim();
    }

    public String getOriginValue() {
        return originInput.inputValue();
    }

    public String getDestinationValue() {
        return destinationInput.inputValue();
    }

    // ---------------- Fechas

    // El popup a veces se abre y se cierra solo justo después de capturar el destino,
    // así que se reintenta la apertura un par de veces hasta ver el calendario.
    public void openDatePicker() {
        for (int attempt = 1; attempt <= MAX_OPEN_ATTEMPTS; attempt++) {
            if (!isExpanded(departureDateButton)) {
                departureDateButton.click();
            }
            try {
                calendar.first().waitFor(new Locator.WaitForOptions().setTimeout(SHORT_WAIT_MS));
                return;
            } catch (TimeoutError e) {
                if (attempt == MAX_OPEN_ATTEMPTS) {
                    throw e;
                }
            }
        }
    }

    public void selectDepartureDate(LocalDate date) {
        openDatePicker();
        selectDayInCalendar(date);
    }

    public void selectReturnDate(LocalDate date) {
        openDatePicker();
        selectDayInCalendar(date);
    }

    public boolean isDatesDoneEnabled() {
        return !isDisabled(datesDoneButton);
    }

    public String getDatesDoneButtonText() {
        return datesDoneButton.textContent().trim();
    }

    public void confirmDates() {
        datesDoneButton.click();
        datePopup.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
    }

    public String getDepartureDateText() {
        return departureDateButton.textContent().trim();
    }

    public String getReturnDateText() {
        return returnDateButton.textContent().trim();
    }

    // Cada día trae la clase automation-date-picker-day-YYYY-M-D (sin ceros a la izquierda).
    // Si el mes no está a la vista se avanza con "Next Month"; año y mes van en la clase.
    private void selectDayInCalendar(LocalDate date) {
        Locator day = datePopup.locator(dayLocator(date)).locator("visible=true");
        int monthsNavigated = 0;
        while (day.count() == 0) {
            if (monthsNavigated++ >= MAX_MONTHS_TO_NAVIGATE || isDisabled(nextMonthButton)) {
                throw new IllegalStateException("La fecha " + date + " no está disponible en el calendario");
            }
            nextMonthButton.click();
        }
        if (isDisabled(day)) {
            throw new IllegalStateException("La fecha " + date + " no es seleccionable (deshabilitada por el sitio)");
        }
        day.click();
    }

    private static String dayLocator(LocalDate date) {
        return ".automation-date-picker-day-%d-%d-%d".formatted(
                date.getYear(), date.getMonthValue(), date.getDayOfMonth());
    }

    // ---------------- Búsqueda

    public String getSearchButtonText() {
        return searchButton.textContent().trim();
    }

    public void clickSearch() {
        searchButton.click();
    }

    // ---------------- Utilidades

    private static int attributeAsInt(Locator locator, String attribute, int defaultValue) {
        String value = locator.getAttribute(attribute);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return Integer.parseInt(value.trim());
    }
}
