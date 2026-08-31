package com.automation.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class ViajesPage {
   private final Page page;
   private final Locator tipoViaje;
   private final Locator tipoClase;
   private final Locator tipoPrimeraClase;

    public ViajesPage(Page page){
    this.page=page;
    this.tipoViaje=page.locator("//*[@id='ROUND_TRIP']");
    this.tipoClase=page.locator("//*[@id='flight-class-dropdown']");
     this.tipoPrimeraClase=page.locator("//div/ul[@role='listbox']/li[@value='FIRST']");

} 

    public void navigateTo() {
        page.navigate("https://www.americanexpress.com/en-us/travel/flights");
    }

    public void selecTipoViaje() {
        tipoViaje.click();
    }

    public void selecTipoClase() {
        tipoClase.click();
        tipoPrimeraClase.click();
    }

}
