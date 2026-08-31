package com.automation.steps;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.automation.pages.ViajesPage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;

public class AmexFlowSteps {
    private Playwright playwright;
    private Browser browser;
    private Page page;
    private ViajesPage viajes;

    @Given("El usuario abre el navegador")
    public void elUsuarioAbreElNavegador() {
        playwright = Playwright.create();
        // Cambia .setHeadless(false) para ver el navegador físicamente
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
        page = browser.newPage();
        viajes=new ViajesPage(page);
    }

    @When("Ingresa al site de Amex")
    public void ingresaALapaginaDeAmex() {
        viajes.navigateTo();
    }

    @When("Selecciona tipo de Viaje")
    public void selecTipoViaje() {
        viajes.selecTipoViaje();
    }

     @When("Selecciona tipo de Clase")
    public void selecTipoClase() {
        viajes.selecTipoClase();
    }
}
