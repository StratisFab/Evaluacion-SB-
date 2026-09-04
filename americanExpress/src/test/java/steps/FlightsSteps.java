package steps;
import com.microsoft.playwright.*;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import pages.FlightsPage;


import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class FlightsSteps{
    private Playwright playwright;
    private Browser browser;
    private Page page;
    private FlightsPage flightsPage;

    private LocalDate departureDate;

    @Before
    public void seUp(){
        playwright = Playwright.create();

        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                        .setHeadless(false)
        );
        page = browser.newPage();
        flightsPage = new FlightsPage(page);

    }

    @Given("the user opens the American Express flights page")
    public void openFlightsPage() {
        flightsPage.open();
    }

    @When("the user selects round trip")
    public void selectRoundTrip() {
        flightsPage.selectRoundTrip();
    }

    @And("the user selects first class")
    public void selectFirstClass() {
        flightsPage.selectFirstClass();
    }

    @And("the user opens travelers")
    public void openTravelers() {
        flightsPage.openTravelers();
    }

    @Then("the decrease adult button should be disabled")
    public void validateDecreaseAdults() {
        assertTrue(flightsPage.isDecreaseAdultsDisabled());
    }

    @When("the user closes travelers")
    public void loseTravelers() {
        flightsPage.closeTravelers();
    }

    @And("the user enters origin {string}")
    public void enterOrigin(String origin) {
        flightsPage.enterOrigin(origin);
    }

    @And("selects the first origin option")
    public void selectOriginOption() {
        flightsPage.selectFirstOriginOption();
    }

    @And("the user enters destination {string}")
    public void enterDestination(String destination) {
        flightsPage.enterDestination(destination);
    }

    @And("selects the first destination option")
    public void selectDestinationOption() {
        flightsPage.selectFirstDestinationOption();
    }

    @And("the user selects a departure date after today")
    public void selectDepartureDate() {
        departureDate = flightsPage.selectDepartureDate();
    }

    @And("the user selects a return date after departure")
    public void selectReturnDate() {
        flightsPage.selectReturnDate(departureDate);
    }

    @And("the user confirms the dates")
    public void confirmDates() {
        flightsPage.confirmDates();
    }

    @And("the user clicks Search")
    public void clickSearch() {
        flightsPage.clickSearch();
    }

    @Then("the title {string} should be displayed")
    public void validateLoginTitle(String expectedTitle) {
        assertTrue(flightsPage.isLoginTitleVisible());
    }

    @After
    public void tearDown(){
        if(browser!= null){
            browser.close();
        }
        if(playwright!=null){
            playwright.close();
        }

    }


}