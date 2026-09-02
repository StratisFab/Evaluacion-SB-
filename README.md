# Prueba técnica Stratis – Búsqueda de vuelos en Amex Travel

Automatización del buscador de vuelos de American Express Travel con Java 21, Gradle 8.4,
Playwright y Cucumber (Gherkin en español), usando Page Object Model.

## Flujo que se automatiza

1. Abrir https://www.americanexpress.com/en-us/travel/flights
2. Tipo de viaje: Viaje de Ida y Vuelta (Round Trip).
3. Clase: Primera Clase (First Class).
4. Viajeros: 1 adulto y validar que el botón (-) esté deshabilitado. Hecho (Done).
5. ¿De quién? (From?): escribir `Mex` y tomar la primera opción -> Ciudad de México (MEX).
6. ¿A? (To?): escribir `Cancún` y tomar la primera opción -> Cancún (CUN).
7. Salida: una fecha posterior a hoy. Regreso: una fecha posterior a la salida. Hecho.
8. Búsqueda (Search) y validar el título "Iniciar sesión en mi cuenta" (*Log in to my account*).

El sitio está en inglés; los textos en español del ejercicio son la traducción del navegador.
Los features van en español y los enums `TripType`, `CabinClass` y `UiButton` traducen esas
etiquetas al texto real de la página.

Hay un segundo escenario de regresión para el contador de adultos: con 2 adultos el (-) se
habilita y al regresar a 1 se vuelve a deshabilitar.

## Estructura

```
src/test
├── java
│   ├── core/                 ConfigReader, PlaywrightManager (ThreadLocal, trace), TestContext (DI)
│   ├── page_objects/         BasePage, FlightsSearchPage, LoginPage, TripType, CabinClass, UiButton
│   ├── steps_definitions/    FlightSearchSteps, Hooks
│   └── runners/              TestRunner (JUnit 5 Platform + Cucumber)
└── resources
    ├── feature/busqueda_vuelos.feature
    ├── configuration/configuration.properties
    └── junit-platform.properties     (glue y plugins al correr el .feature desde IntelliJ)
```

- Feature: qué se prueba. Steps: llaman a los page objects y hacen las aserciones.
  Page objects: localizadores y acciones, sin aserciones. Core: configuración y navegador.
- `TestContext` se inyecta por constructor (cucumber-picocontainer), una instancia por escenario.
  Así varios archivos de steps comparten page objects y datos sin estáticos.

## Selectores

Ids del formulario, `data-testid` y roles/nombres ARIA. Sin xpath posicional.

Cosas del sitio que hubo que resolver:

- Akamai responde "Access Denied" al user agent de Chromium headless ("HeadlessChrome"). En
  headless el framework manda el user agent normal de Chrome (configurable con `userAgent`).
- Tiene `eval` deshabilitado: no sirve `evaluate()` ni `allTextContents()`. Se usa
  `textContent()`, `inputValue()`, `getAttribute()`, `isDisabled()`.
- Los controles se deshabilitan con `aria-disabled`, no con `disabled`.
- El calendario no cuelga de `#date_popup` sino del contenedor `date-picker-popup`, y viene
  duplicado (escritorio y móvil). Se filtra con `visible=true`.
- Cada día del calendario trae la clase `automation-date-picker-day-YYYY-M-D`; con eso se
  selecciona la fecha y se navega de mes si hace falta (también funciona en cambio de año).
- El popup de fechas a veces se abre y se cierra solo al terminar de capturar el destino; la
  apertura se reintenta hasta 3 veces.
- La lista de sugerencias se sincroniza esperando a que la primera opción traiga el código IATA
  esperado, para no tomar resultados de la consulta anterior.
- El tipo de viaje es un grupo de radios en escritorio y un dropdown en pantallas chicas; el
  page object soporta los dos.

## Requisitos

Java 21 (`JAVA_HOME`), Gradle 8.4 o el wrapper, IntelliJ con los plugins Cucumber for Java y
Gherkin. Los navegadores de Playwright se descargan solos en la primera corrida
(o `gradlew playwright --args="install chromium"`).

## Correr por consola

```
gradlew.bat test                                  (Windows)
./gradlew test                                    (Linux/macOS)

gradlew test -Dheadless=true
gradlew test -Dbrowser=firefox                    chromium | firefox | webkit
gradlew test -Dchannel=chrome                     usar el Chrome instalado
gradlew test -DslowMo=300                         ms entre acciones, para ver qué pasa
gradlew test -DblockMedia=true                    sin imágenes/fuentes/video, carga más rápido
gradlew test -DtraceMode=always                   off | on-failure | always
gradlew test -Dcucumber.filter.tags=@smoke
```

## Correr desde IntelliJ

1. File > Open y elegir la carpeta del proyecto (donde está `build.gradle`). Esperar la
   sincronización de Gradle.
2. Settings > Build, Execution, Deployment > Build Tools > Gradle: Gradle JVM = Java 21.
3. Cualquiera de estas:
   - Clic derecho en `runners/TestRunner.java` > Run.
   - Abrir `feature/busqueda_vuelos.feature` y usar el play junto a `Característica` o a un
     `Escenario` (plugin Cucumber for Java; toma el glue de `junit-platform.properties`).
   - Panel Gradle > Tasks > verification > test.
4. Para pasar opciones desde el IDE: Run > Edit Configurations > VM options, p. ej.
   `-Dheadless=true -DtraceMode=always`.

## Reportes y evidencias

- Cucumber: `build/reports/cucumber/cucumber.html` (y `.json`)
- Gradle: `build/reports/tests/test/index.html`
- Si un escenario falla: captura en `build/screenshots/`, URL en el reporte y trace de Playwright
  en `build/traces/` (se abre con `gradlew playwright --args="show-trace build/traces/<archivo>.zip"`).

## Notas de diseño

- Un navegador por hilo y un contexto nuevo por escenario: lanzar el navegador cuesta segundos,
  un contexto cuesta milisegundos y mantiene el aislamiento.
- Sin `sleep` ni `waitForTimeout`; todo se sincroniza por estado (auto-wait, `waitFor`,
  `waitForCondition`).
- Fechas relativas (`departureOffsetDays`, `tripLengthDays`) para que el escenario no caduque.
- Adultos validados contra `min`/`max` del control y días deshabilitados reportados con mensaje
  claro, en lugar de esperar al timeout.
- Paralelismo listo: descomentar `cucumber.execution.parallel.*` en `junit-platform.properties`.
- Con la variable de entorno `CI` definida corre headless solo.
