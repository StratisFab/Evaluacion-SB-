# language: es
@vuelos
Característica: Búsqueda de vuelos en American Express Travel
  Como usuario de American Express Travel
  Quiero buscar un vuelo redondo en primera clase de Ciudad de México a Cancún
  Para que el sitio me solicite iniciar sesión antes de mostrar los resultados

  Antecedentes:
    Dado que el usuario ingresa a la página de vuelos de American Express Travel

  @regresion @smoke
  Escenario: Búsqueda de vuelo de ida y vuelta en primera clase para un adulto
    Cuando selecciona el tipo de viaje "Viaje de Ida y Vuelta"
    Y selecciona la clase "Primera Clase"
    Y selecciona 1 adulto como viajero
    Entonces el botón para disminuir adultos debe estar deshabilitado
    Cuando confirma la selección de viajeros con "Hecho"
    Y ingresa "Mex" en el origen y selecciona la primera opción "Ciudad de México (MEX)"
    Y ingresa "Cancún" en el destino y selecciona la primera opción "Cancún (CUN)"
    Y selecciona una fecha de salida posterior a la fecha actual
    Y selecciona una fecha de regreso posterior a la fecha de salida
    Y confirma la selección de fechas con "Hecho"
    Y presiona el botón "Búsqueda"
    Entonces se debe mostrar el título "Iniciar sesión en mi cuenta"

  @regresion
  Escenario: El botón de disminuir adultos solo se habilita cuando hay más de un adulto
    Cuando selecciona 2 adultos como viajeros
    Entonces el botón para disminuir adultos debe estar habilitado
    Cuando selecciona 1 adulto como viajero
    Entonces el botón para disminuir adultos debe estar deshabilitado
    Cuando confirma la selección de viajeros con "Hecho"
    Entonces el resumen de viajeros debe indicar 1 viajero
