package page_objects;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * Page Object del buscador de vuelos de American Express Travel
 * (https://www.americanexpress.com/en-us/travel/flights).
 * <p>
 * Los localizadores privilegian ids estables, {@code data-testid} y roles/nombres
 * accesibles (ARIA) sobre estructuras HTML o posiciones.
 */
public class FlightsSearchPage extends BasePage {

    private static final int MAX_MONTHS_TO_NAVIGATE = 12;
    private static final int MAX_OPEN_ATTEMPTS = 3;
    private static final double SHORT_WAIT_MS = 5_000;

    // ---- Tipo de viaje -------------------------------------------------------
    private final Locator tripTypeControl;

    // ---- Clase ----------------------------------------------------------------
    private final Locator cabinClassDropdown;

    // ---- Viajeros -------------------------------------------------------------
    private final Locator travelersButton;
    private final Locator adultsRow;
    private final Locator adultsInput;
    private final Locator decreaseAdultsButton;
    private final Locator increaseAdultsButton;
    private final Locator travelersDoneButton;

    // ---- Origen / destino -------------------------------------------------------
    private final Locator originInput;
    private final Locator destinationInput;
    private final Locator suggestionsListbox;
    private final Locator suggestionOptions;

    // ---- Fechas -----------------------------------------------------------------
    private final Locator departureDateButton;
    private final Locator returnDateButton;
    private final Locator datePopup;
    private final Locator calendar;
    private final Locator nextMonthButton;
    private final Locator datesDoneButton;

    // ---- Búsqueda ----------------------------------------------------------------
    private final Locator searchButton;

    public FlightsSearchPage(Page page) {
        super(page);

        tripTypeControl = page.getByTestId("trip-type-segmented-control");

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
        datePopup = page.locator("#date_popup");
        calendar = datePopup.getByTestId("date-picker-calendar");
        nextMonthButton = datePopup.locator("#nextMonthBtn").locator("visible=true");
        datesDoneButton = datePopup.locator("#doneBtn");

        searchButton = page.locator("#axp-travel-search-flights_searchButton");
    }

    // =========================================================================
    // Navegación
    // =========================================================================

    public void open(String url) {
        navigateTo(url);
        searchButton.waitFor();
    }

    // =========================================================================
    // Tipo de viaje y clase
    // =========================================================================

    public void selectTripType(TripType tripType) {
        tripTypeControl
                .getByRole(AriaRole.RADIO, new Locator.GetByRoleOptions().setName(tripType.accessibleName()))
                .click();
    }

    public boolean isTripTypeSelected(TripType tripType) {
        Locator radio = tripTypeControl.getByRole(AriaRole.RADIO,
                new Locator.GetByRoleOptions().setName(tripType.accessibleName()));
        return "true".equals(radio.getAttribute("aria-checked"));
    }

    public void selectCabinClass(CabinClass cabinClass) {
        cabinClassDropdown.click();
        page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName(cabinClass.accessibleName()))
                .click();
    }

    public String getSelectedCabinClass() {
        return cabinClassDropdown.textContent().trim();
    }

    // =========================================================================
    // Viajeros
    // =========================================================================

    public void openTravelersPanel() {
        if (!isExpanded(travelersButton)) {
            travelersButton.click();
        }
        adultsRow.waitFor();
    }

    /** Ajusta el número de adultos usando los botones +/- hasta llegar a la cantidad deseada. */
    public void setAdults(int desired) {
        openTravelersPanel();
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

    public void confirmTravelers() {
        travelersDoneButton.click();
        adultsRow.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
    }

    // =========================================================================
    // Origen y destino
    // =========================================================================

    public void typeOrigin(String text) {
        originInput.click();
        originInput.fill(text);
        suggestionOptions.first().waitFor();
    }

    public void typeDestination(String text) {
        destinationInput.click();
        destinationInput.fill(text);
        suggestionOptions.first().waitFor();
    }

    /**
     * Selecciona la primera sugerencia de la lista de autocompletado y espera a que la
     * lista se cierre, para que la siguiente acción no compita con su animación de cierre.
     */
    public void selectFirstSuggestion() {
        suggestionOptions.first().click();
        suggestionsListbox.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
    }

    /** Devuelve el texto de la primera sugerencia (para validar que corresponde a la ciudad esperada). */
    public String getFirstSuggestionText() {
        return suggestionOptions.first().textContent().trim();
    }

    public String getOriginValue() {
        return originInput.inputValue();
    }

    public String getDestinationValue() {
        return destinationInput.inputValue();
    }

    // =========================================================================
    // Fechas
    // =========================================================================

    /**
     * Abre el selector de fechas si aún no está abierto. El sitio a veces abre y cierra el
     * popup por sí mismo al terminar de capturar el destino, por lo que se reintenta la
     * apertura un número limitado de veces hasta que el calendario quede visible.
     */
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

    /**
     * Cada día del calendario expone una clase pensada para automatización:
     * {@code automation-date-picker-day-YYYY-M-D} (mes y día sin ceros a la izquierda).
     * Si el mes no está visible, se avanza con "Next Month" hasta encontrarlo.
     */
    private void selectDayInCalendar(LocalDate date) {
        Locator day = datePopup.locator(dayLocator(date));
        int attempts = 0;
        while (day.count() == 0 && attempts < MAX_MONTHS_TO_NAVIGATE) {
            nextMonthButton.click();
            attempts++;
        }
        if (day.count() == 0) {
            throw new IllegalStateException("No se encontró la fecha " + date + " en el calendario");
        }
        day.click();
    }

    private static String dayLocator(LocalDate date) {
        return ".automation-date-picker-day-%d-%d-%d".formatted(
                date.getYear(), date.getMonthValue(), date.getDayOfMonth());
    }

    // =========================================================================
    // Búsqueda
    // =========================================================================

    public void clickSearch() {
        searchButton.click();
    }
}
