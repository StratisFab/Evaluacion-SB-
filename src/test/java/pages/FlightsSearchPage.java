package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import config.ConfigReader;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Pattern;

public class FlightsSearchPage extends BasePage {

    private static final Pattern ROUND_TRIP = Pattern.compile("Round Trip|Viaje de Ida y Vuelta|Ida y vuelta", Pattern.CASE_INSENSITIVE);
    private static final Pattern CLASS_TRIGGER = Pattern.compile("(Economy|Premium Economy|Business|First) Class|Clase", Pattern.CASE_INSENSITIVE);
    private static final Pattern FIRST_CLASS = Pattern.compile("First Class|Primera [Cc]lase");
    private static final Pattern TRAVELERS = Pattern.compile("\\d+\\s+Traveler.*|.*Viajero.*", Pattern.CASE_INSENSITIVE);
    private static final Pattern DECREASE_ADULTS = Pattern.compile(
            "((decrease|subtract|remove|minus|menos|disminuir).*(adult|adulto))|((adult|adulto).*(decrease|menos|disminuir))",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern DECREASE = Pattern.compile("^decrease$|^disminuir$|^menos$", Pattern.CASE_INSENSITIVE);
    private static final Pattern DONE = Pattern.compile("^Done$|^Hecho$");
    private static final Pattern FROM = Pattern.compile("From\\?|¿De quién\\?|¿De dónde\\?");
    private static final Pattern TO = Pattern.compile("To\\?|¿A\\?");
    private static final Pattern DEPART = Pattern.compile("^Depart$|^Salida$");
    private static final Pattern SEARCH = Pattern.compile("Search flights|^Search$|^Búsqueda$");

    private LocalDate departureDate;

    public void open() {
        navigate(ConfigReader.get("app.baseUrl"));
        byRole(AriaRole.RADIO, ROUND_TRIP).waitFor();
    }

    public void selectRoundTrip() {
        click(byRole(AriaRole.RADIO, ROUND_TRIP));
    }

    public void selectFirstClass() {
        click(byRole(AriaRole.BUTTON, CLASS_TRIGGER));
        click(byRole(AriaRole.OPTION, FIRST_CLASS)
                .or(byRole(AriaRole.BUTTON, FIRST_CLASS))
                .or(page.getByText(FIRST_CLASS)));
    }

    public void openTravelers() {
        click(byRole(AriaRole.BUTTON, TRAVELERS));
        adultDecreaseButton().waitFor();
    }

    public Locator adultDecreaseButton() {
        return byRole(AriaRole.BUTTON, DECREASE_ADULTS)
                .or(byRole(AriaRole.BUTTON, DECREASE));
    }

    public void confirmTravelers() {
        click(byRole(AriaRole.BUTTON, DONE));
    }

    public void selectOriginFirstOption(String query) {
        chooseAirport(byRole(AriaRole.COMBOBOX, FROM), query);
    }

    public void selectDestinationFirstOption(String query) {
        chooseAirport(byRole(AriaRole.COMBOBOX, TO), query);
    }

    public void selectDepartureAfterToday() {
        departureDate = LocalDate.now().plusDays(ConfigReader.getInt("flight.departure.days.from.today"));
        click(byRole(AriaRole.BUTTON, DEPART));
        waitForCalendar();
        selectCalendarDate(departureDate);
    }

    public void selectReturnAfterDeparture() {
        if (departureDate == null) {
            departureDate = LocalDate.now().plusDays(ConfigReader.getInt("flight.departure.days.from.today"));
        }
        LocalDate returnDate = departureDate.plusDays(ConfigReader.getInt("flight.return.days.after.departure"));
        selectCalendarDate(returnDate);
    }

    public void confirmDates() {
        calendarPopup().getByText("Done", new Locator.GetByTextOptions().setExact(true)).click();
    }

    public void searchFlights() {
        click(byRole(AriaRole.BUTTON, SEARCH));
    }

    private void chooseAirport(Locator field, String query) {
        typeInto(field, query);
        Locator option = firstOption();
        option.waitFor();
        click(option);
    }

    private void waitForCalendar() {
        page.getByText(currentMonthYear(), new Page.GetByTextOptions().setExact(true)).first().waitFor();
    }

    private void selectCalendarDate(LocalDate date) {
        String day = String.valueOf(date.getDayOfMonth());
        calendarPopup()
                .getByText(day, new Locator.GetByTextOptions().setExact(true))
                .first()
                .click();
    }

    private Locator calendarPopup() {
        return page.getByText(currentMonthYear(), new Page.GetByTextOptions().setExact(true))
                .first()
                .locator("xpath=ancestor::div[.//*[normalize-space()='Done' or normalize-space()='Hecho']][1]");
    }

    private String currentMonthYear() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.US));
    }
}
