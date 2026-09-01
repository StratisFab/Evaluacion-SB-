Feature: Viaje de ida y vuelta

  Scenario: Primera clase
    Given el usuario está en la página de vuelos de American Express Travel
    When el usuario selecciona viaje de ida y vuelta
    And el usuario selecciona primera clase
    And el usuario selecciona un adulto
    Then el botón de disminuir adultos está deshabilitado
    And el usuario confirma los viajeros
