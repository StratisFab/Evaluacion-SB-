Feature: Búsqueda de vuelos

  Background:
    Given el usuario está en la página de vuelos de American Express Travel
    When el usuario selecciona viaje de ida y vuelta
    And el usuario selecciona primera clase
    And el usuario selecciona un adulto
    And el usuario confirma los viajeros

  Scenario: Cotizar de Ciudad de México a Cancún
    When el usuario ingresa "Mex" en origen y selecciona la primera opción
    And el usuario ingresa "Cancún" en destino y selecciona la primera opción
    And el usuario selecciona una fecha de salida posterior a hoy
    And el usuario selecciona una fecha de regreso posterior a la salida
    And el usuario confirma las fechas
    And el usuario selecciona búsqueda
    Then se muestra el título "Iniciar sesión en mi cuenta"
