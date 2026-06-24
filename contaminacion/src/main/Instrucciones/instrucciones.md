# ==============================================================================
# SISTEMA INTEGRAL DE GESTIÓN Y PREDICCIÓN DE CONTAMINACIÓN DEL AIRE (QUITO - 5 ZONAS)
# ==============================================================================
# Framework de Software Web: Java + Vaadin + MongoDB (Docker) + POO
# Configuración: Exclusivo para 5 zonas de monitoreo y 5 contaminantes (con O3).


--------------------------------------------------------------------------------
1. ESPECIFICACIÓN DE REQUERIMIENTOS
--------------------------------------------------------------------------------

[REQUERIMIENTOS FUNCIONALES (RF)]

* RF1.1 Dashboard Principal:
  La interfaz web debe mostrar un Grid<ZonaUrbana> interactivo limitado
  estrictamente a las 5 zonas principales de la ciudad de Quito:
    1. Zona Norte (Belisario / Cotocollao)
    2. Zona Centro (Centro Histórico)
    3. Zona Sur (El Camal / Las Cuadras)
    4. Zona Valles (Tumbaco / Cumbayá)
    5. Zona Noroccidente (Carapungo / Calderón)
       El Grid exhibirá sus niveles en tiempo real de CO2, SO2, NO2, PM2.5 y O3.

* RF1.2 Registro de Datos:
  Formulario web para actualizar los niveles de contaminación y factores
  climáticos actuales de una de las 5 zonas seleccionada.

* RF1.3 Persistencia Inmediata:
  Los datos modificados deben guardarse automáticamente en la base de datos
  local de MongoDB bajo el identificador único de la zona.

* RF2.1 Historial y Límites OMS:
  El sistema debe calcular el promedio de contaminación de los últimos 30 días
  para la zona seleccionada y compararlo con los estándares diarios de la OMS.

* RF2.2 Algoritmo de Predicción:
  Al presionar un botón, el sistema proyectará la calidad del aire para las
  próximas 24 horas aplicando un modelo de regresión lineal múltiple basado
  en el clima actual de esa zona.

* RF2.3 Alertas Visuales:
  Si el estado actual o la predicción de 24h de cualquiera de las 5 zonas
  superan los límites de la OMS, Vaadin mostrará un 'Notification' o 'Dialog'.

* RF3.1 Recomendaciones Adaptativas:
  Ejecución de medidas de mitigación mediante polimorfismo según el tipo de zona:
    - Zonas Industriales (ej. Sectores asignados en Zona Norte/Sur): Reducción de
      producción o cierre temporal de fábricas.
    - Zonas Residenciales/Comerciales (ej. Centro / Valles): Activación de
      restricción vehicular (Pico y Placa) o suspensión de actividades al aire libre.

* RF3.2 Exportación de Reportes:
  Botón para generar y descargar un archivo físico (.txt o .csv) con el
  resumen de las 5 zonas, sus predicciones y alertas activas.


[REQUERIMIENTOS NO FUNCIONALES (RNF)]

* RNF1 Arquitectura Limpia:
  Separación estricta de responsabilidades en capas (Modelos, Interfaces,
  Repositorios, Servicios, Vistas).

* RNF2 Infraestructura:
  Motor de base de datos obligatorio MongoDB corriendo localmente en un
  contenedor de Docker (Puerto 27017).

* RNF3 Control de Errores:
  Intercepción de fallos mediante excepciones personalizadas
  (DatosInvalidosException, DatabaseConnectionException, LimitesExcedidosException).

* RNF4 Optimización:
  Gestión de conexión a MongoDB mediante el patrón de diseño Singleton.

* RNF5 Interfaz de Usuario:
  Interacción 100% web construida con el framework Vaadin.


--------------------------------------------------------------------------------
2. HOJA DE RUTA DEL DESARROLLO (ROADMAP)
--------------------------------------------------------------------------------

* FASE 1: Estructuración del Núcleo (Modelos y Abstracción)
    1. Crear clases de datos estándar: Clima y Contaminacion (con los 5 contaminantes).
    2. Crear la clase madre abstracta ZonaUrbana con atributos private y el
       método abstracto aplicarMedidasMitigacion().
    3. Crear las clases hijas concretas ZonaIndustrial y ZonaResidencial para
       mapear las 5 zonas del sistema.

* FASE 2: Infraestructura y Persistencia (Docker + MongoDB)
    1. Levantar el contenedor de Docker con MongoDB local.
    2. Crear la clase MongoConnection (Patrón Singleton).
    3. Diseñar la Interface ZonaRepository y su clase de implementación real
       ZonaRepositoryMongo (restringido a los 5 documentos/IDs de zona).

* FASE 3: Capa de Lógica de Negocio y Servicios
    1. Diseñar la Interface PrediccionService y su implementación
       CalculadorPrediccion (Cálculo de regresión y promedios sobre el histórico).
    2. Diseñar la Interface ReporteService para la generación del archivo consolidado.

* FASE 4: Blindaje del Sistema (Excepciones)
    1. Crear las excepciones personalizadas (Exception).
    2. Implementar validaciones en los modelos para lanzar errores ante datos incoherentes.

* FASE 5: Capa de Presentación (Interfaz Web Vaadin)
    1. Diseñar la vista principal (MainView) con el Grid fijo de 5 filas (las 5 zonas).
    2. Implementar formularios de entrada de datos y enlazar los botones a
       los servicios correspondientes.
    3. Configurar alertas visuales (Dialog) ante excepciones de límites excedidos.


--------------------------------------------------------------------------------
3. DISEÑO DE MODELOS DE DATOS (POO)
--------------------------------------------------------------------------------

[Clima (Java Class)]
* Atributos: private double temperatura, private double velocidadViento,
  private double humedad.
* Estructura: Constructor completo, Getters y Setters con validaciones.

[Contaminacion (Java Class)]
* Atributos: private double co2, private double so2, private double no2,
  private double pm25, private double o3, private LocalDate fecha.
* Estructura: Constructor completo, Getters y Setters (No permite valores negativos).

[ZonaUrbana (Abstract Class)]
* Atributos: private String id, private String nombre, private Clima climaActual,
  private Contaminacion contaminacionActual, private List<Contaminacion> historico30Dias.
* Métodos comunes: public void agregarRegistroHistorico(Contaminacion r).
* Método abstracto: public abstract void aplicarMedidasMitigacion().

[ZonaIndustrial (Java Class - Hija)]
* Atributos propios: private int numeroFabricasActivas.
* Polimorfismo: Implementación de @Override public void aplicarMedidasMitigacion().

[ZonaResidencial (Java Class - Hija)]
* Atributos propios: private boolean restriccionVehicularActiva.
* Polimorfismo: Implementación de @Override public void aplicarMedidasMitigacion().


--------------------------------------------------------------------------------
4. ESTRUCTURA LÓGICA DE LOS CÁLCULOS
--------------------------------------------------------------------------------

[A. Modelo de Predicción Futura (C_t+24)]
Se implementará en la clase CalculadorPrediccion aplicando la ecuación de
regresión lineal múltiple para cada uno de los contaminantes:

C_t+24 = b0 + b1(C_t-24) + b2(Temp) + b3(Hum) + b4(Vel_viento) + b5(Emisiones) + e

Ponderación de Coeficientes (b):
* b1 (Tendencia Histórica): 0.6 (Alta dependencia del estado previo).
* b2 (Temperatura): 0.1 (Crítico para el O3, la radiación y temperatura catalizan su formación).
* b3 (Humedad): 0.05 (Aumento de peso de partículas).
* b4 (Velocidad del Viento): -0.25 (Negativo, representa la dispersión en la hoya de Quito).
* b5 (Emisiones de la Zona): Depende de fábricas activas o tráfico según la zona de las 5 configuradas.


[B. Cálculo de Promedios Históricos (30 Días)]
* El servicio recorrerá la lista historico30Dias de la zona seleccionada.
* Sumará las concentraciones de los 5 contaminantes y las dividirá entre 30.0.
* Comparará el resultado promediado contra las directrices oficiales de la OMS:
    - PM2.5: 15 ug/m3
    - NO2:   25 ug/m3
    - SO2:   40 ug/m3
    - O3:    100 ug/m3 (Máxima diaria de la media de 8 horas)
    - CO2:   4.0 mg/m3 (Métrica adaptada de la directriz diaria de la OMS para CO)


--------------------------------------------------------------------------------
5. CONFIGURACIÓN DE INFRAESTRUCTURA LOCAL (DOCKER)
--------------------------------------------------------------------------------

Para levantar la base de datos de MongoDB de manera local y persistente donde
se almacenarán los documentos de las 5 zonas de Quito, ejecute:
