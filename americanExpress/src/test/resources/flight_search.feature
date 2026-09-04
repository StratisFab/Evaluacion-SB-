Feature: Search American Express flights

  Scenario: Search round trip first class flight
    Given the user opens the American Express flights page
    When the user selects round trip
    And the user selects first class
    And the user opens travelers
    Then the decrease adult button should be disabled
    When the user closes travelers
    And the user enters origin "Mex"
    And selects the first origin option
    And the user enters destination "Cancún"
    And selects the first destination option
    And the user selects a departure date after today
    And the user selects a return date after departure
    And the user confirms the dates
    And the user clicks Search
    Then the title "Iniciar sesión en mi cuenta" should be displayed