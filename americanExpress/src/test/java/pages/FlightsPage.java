package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Pattern;

public class FlightsPage {

    private final Page page;

    public FlightsPage(Page page) {
        this.page = page;
    }

    public void open() {
        page.navigate("https://www.americanexpress.com/en-us/travel/flights");
        page.waitForLoadState();
    }

    public void selectRoundTrip() {

        Locator roundTrip = page.getByText(
                Pattern.compile("^Round Trip$", Pattern.CASE_INSENSITIVE)
        );

        getVisibleLocator(roundTrip).click();
    }

    public void selectFirstClass() {

        Locator economy = page.getByText(
                Pattern.compile("^Economy$", Pattern.CASE_INSENSITIVE)
        );

        Locator visibleEconomy = findVisibleLocator(economy);

        if (visibleEconomy != null) {
            visibleEconomy.click();
        } else {

            Locator classLabel = page.getByText(
                    Pattern.compile("^Class$", Pattern.CASE_INSENSITIVE)
            );

            Locator visibleClass = getVisibleLocator(classLabel);

            Locator container = visibleClass.locator("xpath=..");

            Locator clickable = container.locator(
                    "button, [role='button'], [role='combobox']"
            );

            Locator visibleClickable = findVisibleLocator(clickable);

            if (visibleClickable != null) {
                visibleClickable.click();
            } else {
                container.click();
            }
        }

        Locator firstClass = page.getByText(
                Pattern.compile("^First Class$", Pattern.CASE_INSENSITIVE)
        );

        Locator visibleFirstClass = findVisibleLocator(firstClass);

        if (visibleFirstClass != null) {
            visibleFirstClass.click();
            return;
        }

        Locator options = page.locator(
                "[role='option'], li"
        );

        for (int i = 0; i < options.count(); i++) {

            Locator option = options.nth(i);

            if (!option.isVisible()) {
                continue;
            }

            String text = option.innerText();

            if (
                    text != null &&
                            text.trim().equalsIgnoreCase("First Class")
            ) {
                option.click();
                return;
            }
        }

        throw new RuntimeException(
                "No se encontró First Class"
        );
    }

    public void openTravelers() {

        Locator travelers = page.getByText(
                Pattern.compile(
                        "^Travelers$|^1 Traveler$|Travelers.*1 Traveler",
                        Pattern.CASE_INSENSITIVE
                )
        );

        getVisibleLocator(travelers).click();
    }

    public boolean isDecreaseAdultsDisabled() {

        Locator adults = page.getByText(
                Pattern.compile("^Adults?$", Pattern.CASE_INSENSITIVE)
        );

        Locator visibleAdults = findVisibleLocator(adults);

        if (visibleAdults != null) {

            Locator container = visibleAdults.locator("xpath=..");

            for (int level = 0; level < 5; level++) {

                Locator disabled = container.locator(
                        "button:disabled, " +
                                "button[aria-disabled='true'], " +
                                "[role='button'][aria-disabled='true']"
                );

                Locator visibleDisabled =
                        findVisibleLocator(disabled);

                if (visibleDisabled != null) {
                    return true;
                }

                container = container.locator("xpath=..");
            }
        }

        Locator buttons = page.locator(
                "button, [role='button']"
        );

        for (int i = 0; i < buttons.count(); i++) {

            Locator button = buttons.nth(i);

            if (!button.isVisible()) {
                continue;
            }

            String ariaLabel =
                    button.getAttribute("aria-label");

            String title =
                    button.getAttribute("title");

            String text =
                    button.innerText();

            String content =
                    (
                            (ariaLabel == null ? "" : ariaLabel)
                                    + " "
                                    + (title == null ? "" : title)
                                    + " "
                                    + (text == null ? "" : text)
                    ).toLowerCase();

            if (
                    content.contains("adult") &&
                            (
                                    content.contains("decrease") ||
                                            content.contains("minus") ||
                                            content.contains("remove") ||
                                            content.contains("subtract")
                            )
            ) {

                return button.isDisabled()
                        ||
                        button.getAttribute("disabled") != null
                        ||
                        "true".equalsIgnoreCase(
                                button.getAttribute("aria-disabled")
                        );
            }
        }

        Locator visibleDisabledButtons = page.locator(
                "button:visible:disabled, " +
                        "button:visible[aria-disabled='true']"
        );

        return visibleDisabledButtons.count() > 0;
    }

    public void closeTravelers() {

        Locator done = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions()
                        .setName(
                                Pattern.compile(
                                        "Done|Apply|Hecho",
                                        Pattern.CASE_INSENSITIVE
                                )
                        )
        );

        Locator visibleDone = findVisibleLocator(done);

        if (visibleDone != null) {
            visibleDone.click();
            return;
        }

        Locator doneText = page.getByText(
                Pattern.compile(
                        "^Done$|^Apply$|^Hecho$",
                        Pattern.CASE_INSENSITIVE
                )
        );

        visibleDone = findVisibleLocator(doneText);

        if (visibleDone != null) {
            visibleDone.click();
            return;
        }

        page.keyboard().press("Escape");
    }

    public void enterOrigin(String city) {

        Locator input = findAirportInput(
                Pattern.compile(
                        "From|Origin",
                        Pattern.CASE_INSENSITIVE
                ),
                0
        );

        input.click();
        input.fill(city);
    }

    public void selectFirstOriginOption() {

        Locator mexicoCity = page.getByText(
                Pattern.compile(
                        "Mexico City|Ciudad de México",
                        Pattern.CASE_INSENSITIVE
                )
        );

        Locator visibleMexicoCity =
                findVisibleLocator(mexicoCity);

        if (visibleMexicoCity != null) {
            visibleMexicoCity.click();
            return;
        }

        selectFirstVisibleOption();
    }

    public void enterDestination(String city) {

        Locator input = findAirportInput(
                Pattern.compile(
                        "^To$|Destination",
                        Pattern.CASE_INSENSITIVE
                ),
                1
        );

        input.click();
        input.fill(city);
    }

    public void selectFirstDestinationOption() {

        Locator cancun = page.getByText(
                Pattern.compile(
                        "Cancun|Cancún",
                        Pattern.CASE_INSENSITIVE
                )
        );

        Locator visibleCancun =
                findVisibleLocator(cancun);

        if (visibleCancun != null) {
            visibleCancun.click();
            return;
        }

        selectFirstVisibleOption();
    }

    public LocalDate selectDepartureDate() {

        LocalDate departureDate =
                LocalDate.now().plusDays(2);

        Locator depart = page.getByText(
                Pattern.compile(
                        "^Depart$|^Departure$",
                        Pattern.CASE_INSENSITIVE
                )
        );

        getVisibleLocator(depart).click();

        selectDate(departureDate);

        return departureDate;
    }

    public LocalDate selectReturnDate(
            LocalDate departureDate
    ) {

        LocalDate returnDate =
                departureDate.plusDays(3);

        Locator returnControl = page.getByText(
                Pattern.compile(
                        "^Return$",
                        Pattern.CASE_INSENSITIVE
                )
        );

        Locator visibleReturn =
                findVisibleLocator(returnControl);

        if (visibleReturn != null) {
            visibleReturn.click();
        }

        selectDate(returnDate);

        return returnDate;
    }

    public void confirmDates() {

        Locator done = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions()
                        .setName(
                                Pattern.compile(
                                        "Done|Apply|Hecho",
                                        Pattern.CASE_INSENSITIVE
                                )
                        )
        );

        Locator visibleDone =
                findVisibleLocator(done);

        if (visibleDone != null) {
            visibleDone.click();
            return;
        }

        Locator doneText = page.getByText(
                Pattern.compile(
                        "^Done$|^Apply$|^Hecho$",
                        Pattern.CASE_INSENSITIVE
                )
        );

        visibleDone = findVisibleLocator(doneText);

        if (visibleDone != null) {
            visibleDone.click();
        }
    }

    public void clickSearch() {

        Locator search = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions()
                        .setName(
                                Pattern.compile(
                                        "^Search$|^Buscar$",
                                        Pattern.CASE_INSENSITIVE
                                )
                        )
        );

        getVisibleLocator(search).click();
    }

    public boolean isLoginTitleVisible() {

        page.waitForTimeout(1500);

        Locator title = page.getByText(
                Pattern.compile(
                        "Iniciar sesión en mi cuenta|" +
                                "Sign in to my account|" +
                                "Log in to my account",
                        Pattern.CASE_INSENSITIVE
                )
        );

        return findVisibleLocator(title) != null;
    }

    private Locator findAirportInput(
            Pattern fieldPattern,
            int fallbackIndex
    ) {

        Locator inputs = page.locator("input");

        for (int i = 0; i < inputs.count(); i++) {

            Locator input = inputs.nth(i);

            if (!input.isVisible()) {
                continue;
            }

            String ariaLabel =
                    input.getAttribute("aria-label");

            String placeholder =
                    input.getAttribute("placeholder");

            String name =
                    input.getAttribute("name");

            String id =
                    input.getAttribute("id");

            String content =
                    (ariaLabel == null ? "" : ariaLabel)
                            + " "
                            + (placeholder == null ? "" : placeholder)
                            + " "
                            + (name == null ? "" : name)
                            + " "
                            + (id == null ? "" : id);

            if (fieldPattern.matcher(content).find()) {
                return input;
            }
        }

        return getVisibleInputByIndex(
                fallbackIndex
        );
    }

    private Locator getVisibleInputByIndex(
            int requestedIndex
    ) {

        Locator inputs = page.locator("input");

        int visibleIndex = 0;

        for (int i = 0; i < inputs.count(); i++) {

            Locator input = inputs.nth(i);

            if (!input.isVisible()) {
                continue;
            }

            if (visibleIndex == requestedIndex) {
                return input;
            }

            visibleIndex++;
        }

        throw new RuntimeException(
                "No se encontró input visible índice "
                        + requestedIndex
        );
    }

    private void selectFirstVisibleOption() {

        Locator options = page.getByRole(
                AriaRole.OPTION
        );

        Locator visible =
                findVisibleLocator(options);

        if (visible != null) {
            visible.click();
            return;
        }

        Locator alternatives = page.locator(
                "[role='option'], " +
                        "[role='listbox'] li, " +
                        "ul li"
        );

        visible = findVisibleLocator(
                alternatives
        );

        if (visible != null) {
            visible.click();
            return;
        }

        throw new RuntimeException(
                "No se encontró opción de aeropuerto"
        );
    }

    private void selectDate(
            LocalDate date
    ) {

        DateTimeFormatter fullFormatter =
                DateTimeFormatter.ofPattern(
                        "MMMM d, yyyy",
                        Locale.ENGLISH
                );

        String fullDate =
                date.format(fullFormatter);

        Locator ariaDate = page.locator(
                "[aria-label*=\"" + fullDate + "\"]"
        );

        Locator visibleDate =
                findVisibleLocator(ariaDate);

        if (visibleDate != null) {
            visibleDate.click();
            return;
        }

        DateTimeFormatter monthDayFormatter =
                DateTimeFormatter.ofPattern(
                        "MMMM d",
                        Locale.ENGLISH
                );

        String monthDay =
                date.format(monthDayFormatter);

        Locator partialAriaDate =
                page.locator(
                        "[aria-label*=\"" +
                                monthDay +
                                "\"]"
                );

        visibleDate =
                findVisibleLocator(partialAriaDate);

        if (visibleDate != null) {
            visibleDate.click();
            return;
        }

        Locator dateButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions()
                        .setName(
                                Pattern.compile(
                                        Pattern.quote(fullDate),
                                        Pattern.CASE_INSENSITIVE
                                )
                        )
        );

        visibleDate =
                findVisibleLocator(dateButton);

        if (visibleDate != null) {
            visibleDate.click();
            return;
        }

        Locator day = page.getByText(
                Pattern.compile(
                        "^" +
                                date.getDayOfMonth() +
                                "$"
                )
        );

        visibleDate =
                findVisibleLocator(day);

        if (visibleDate != null) {
            visibleDate.click();
            return;
        }

        throw new RuntimeException(
                "No se encontró fecha "
                        + fullDate
        );
    }

    private Locator getVisibleLocator(
            Locator locator
    ) {

        Locator visible =
                findVisibleLocator(locator);

        if (visible == null) {
            throw new RuntimeException(
                    "No se encontró un elemento visible"
            );
        }

        return visible;
    }

    private Locator findVisibleLocator(
            Locator locator
    ) {

        int count = locator.count();

        for (int i = 0; i < count; i++) {

            Locator current =
                    locator.nth(i);

            if (current.isVisible()) {
                return current;
            }
        }

        return null;
    }
}