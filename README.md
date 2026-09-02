# Prueba técnica Stratis – Automatización de búsqueda de vuelos (American Express Travel)

Framework de pruebas end-to-end construido con **Java 21 + Gradle 8.4 + Playwright + Cucumber (BDD)**
aplicando el patrón **Page Object Model (POM)**.

## Flujo automatizado

1. Ingresar a https://www.americanexpress.com/en-us/travel/flights
2. Seleccionar tipo de viaje **Viaje de Ida y Vuelta** (Round Trip).
3. Seleccionar clase **Primera Clase** (First Class).
4. Seleccionar **1 adulto** como viajero y validar que el botón **(-)** de adultos esté deshabilitado.
5. Seleccionar **Hecho** (Done).
6. En **¿De quién?** (From?) escribir `Mex` y elegir la primera opción → Ciudad de México (MEX).
7. En **¿A?** (To?) escribir `Cancún` y elegir la primera opción → Cancún (CUN).
8. Seleccionar una fecha de **Salida** posterior a la fecha actual.
9. Seleccionar una fecha de **Regreso** posterior a la fecha de salida.
10. Seleccionar **Hecho** (Done) en el calendario.
11. Presionar **Búsqueda** (Search).
12. Validar que se muestre el título **Iniciar sesión en mi cuenta** (*Log in to my account*).

> El sitio se despliega en inglés (en-us). Los textos en español del ejercicio corresponden a la
> traducción automática del navegador; los escenarios Gherkin se escriben en español y los Page
> Objects traducen esas etiquetas a los nombres accesibles reales del sitio (`TripType`, `CabinClass`).

## Estructura del proyecto

```
├── build.gradle                         # dependencias y tarea test
├── settings.gradle
├── gradlew / gradlew.bat                # wrapper de Gradle 8.4
└── src/test
    ├── java
    │   ├── core/                        # infraestructura
    │   │   ├── ConfigReader.java        # lectura de configuration.properties / -D
    │   │   └── PlaywrightManager.java   # ciclo de vida Playwright (ThreadLocal)
    │   ├── page_objects/                # Page Object Model
    │   │   ├── BasePage.java
    │   │   ├── FlightsSearchPage.java   # buscador de vuelos
    │   │   ├── LoginPage.java           # pantalla "Log in to my account"
    │   │   ├── TripType.java            # enum tipo de viaje (es -> en)
    │   │   └── CabinClass.java          # enum clase de cabina (es -> en)
    │   ├── steps_definitions/           # BDD: step definitions y hooks
    │   │   ├── FlightSearchSteps.java
    │   │   └── Hooks.java               # abre/cierra navegador, captura en fallo
    │   └── runners/
    │       └── TestRunner.java          # JUnit 5 Platform + Cucumber
    └── resources
        ├── feature/busqueda_vuelos.feature      # escenario Gherkin (español)
        ├── configuration/configuration.properties
        └── junit-platform.properties            # glue/plugins al ejecutar desde IntelliJ
```

Capas y responsabilidades:

| Capa | Responsabilidad |
|---|---|
| `feature` | Describe el comportamiento en Gherkin (qué se prueba). |
| `steps_definitions` | Traduce cada paso a llamadas al Page Object y contiene las aserciones. |
| `page_objects` | Localizadores y acciones de cada pantalla (cómo se interactúa). |
| `core` | Configuración y ciclo de vida del navegador. |

## Selectores

Se privilegian identificadores estables y atributos de accesibilidad, evitando XPath posicional:

- `data-testid` (`trip-type-segmented-control`, `adult-stepper-row`, `date-picker-popup`, ...).
- `id` semánticos del formulario (`flight-class-dropdown`, `axp-travel-search-flights_searchButton`, ...).
- Roles y nombres accesibles (`getByRole(RADIO, "Round Trip")`, `getByRole(OPTION, "First Class")`).
- Días del calendario mediante la clase de automatización que expone el sitio:
  `automation-date-picker-day-YYYY-M-D`.

## Requisitos

- Java 21 (`JAVA_HOME` configurado)
- Gradle 8.4 (o usar el wrapper `gradlew`)
- IntelliJ IDEA con plugins **Cucumber for Java** y **Gherkin**
- Navegadores de Playwright (se descargan automáticamente en la primera ejecución; también:
  `gradlew playwright --args="install chromium"`)

## Ejecución

```bash
# Windows
gradlew.bat test

# Linux / macOS
./gradlew test
```

Opciones por línea de comandos (sobrescriben `configuration.properties`):

```bash
gradlew test -Dheadless=true                 # sin interfaz gráfica
gradlew test -Dbrowser=firefox               # chromium | firefox | webkit
gradlew test -DslowMo=300                    # retardo entre acciones (ms)
gradlew test -Dcucumber.filter.tags=@smoke   # filtrar escenarios por tag
```

Desde IntelliJ: ejecutar `runners.TestRunner` o el archivo `.feature` directamente
(el plugin Cucumber for Java usa `junit-platform.properties`).

## Reportes y evidencias

- Reporte Cucumber HTML: `build/reports/cucumber/cucumber.html`
- Reporte JSON: `build/reports/cucumber/cucumber.json`
- Reporte Gradle: `build/reports/tests/test/index.html`
- Captura de pantalla automática cuando un escenario falla: `build/screenshots/`
