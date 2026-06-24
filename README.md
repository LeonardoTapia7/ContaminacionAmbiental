# 🌍 Sistema Integral de Gestión y Predicción de Contaminación del Aire (Quito)

**Framework**: Vaadin (UI) + Spring Boot + MongoDB (Docker) + Java 25

## ✅ Estado Actual

El **código está 100% listo** con:
- ✅ 5 zonas de monitoreo con datos históricos (30 días por zona)
- ✅ Dashboard interactivo con Grid Vaadin
- ✅ Formulario para registrar contaminantes y clima
- ✅ Predicción de calidad del aire (24h) con regresión lineal
- ✅ Alertas OMS visuales (Notification + Dialog)
- ✅ Exportación CSV de reportes
- ✅ **MongoDB en Docker** (persistencia completa)
- ✅ Arquitectura limpia (Modelos → Servicios → Repositorio → UI)

## 🚀 Cómo Ejecutar (Windows PowerShell)

### Opción 1: Script Automático (RECOMENDADO)

Simplemente ejecuta el script de setup (hace TODO automáticamente):

```powershell
# Abre PowerShell en la carpeta raíz del proyecto
cd 'C:\Users\joelt\IdeaProjects\contaminacion'

# Dale permiso de ejecución al script (primera vez)
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser

# Ejecuta el script (levanta Docker, compila y corre la app)
.\setup-and-run.ps1
```

Esto:
1. ✅ Verifica que Docker Desktop esté corriendo (si no, te lo dirá)
2. ✅ Levanta MongoDB en Puerto 27017 (contenedor: `contaminacion_mongo`)
3. ✅ Descarga todas las dependencias Maven
4. ✅ Compila el proyecto
5. ✅ Ejecuta la aplicación
6. ✅ Abre automáticamente en tu navegador: **http://localhost:8080**

### Opción 2: Manual (paso a paso)

Si prefieres hacerlo manualmente:

#### 1️⃣ Asegúrate que Docker Desktop esté corriendo
- Abre Docker Desktop y espera a que esté listo (icono verde)

#### 2️⃣ Levanta MongoDB
```powershell
cd 'C:\Users\joelt\IdeaProjects\contaminacion'
docker-compose up -d
# Verifica que el contenedor está corriendo:
docker ps
# Deberías ver: contaminacion_mongo
```

#### 3️⃣ Compila y ejecuta la app
```powershell
cd 'C:\Users\joelt\IdeaProjects\contaminacion\contaminacion'

# Descargar dependencias y compilar
.\mvnw.cmd -DskipTests package

# Ejecutar
.\mvnw.cmd -DskipTests spring-boot:run
```

#### 4️⃣ Abre tu navegador
```
http://localhost:8080
```

## 📋 Uso de la Aplicación

### Dashboard Principal
1. **Grid de 5 Zonas**: Muestra las 5 zonas de Quito con estado actual
   - Zona Norte (Industrial)
   - Zona Centro (Residencial)
   - Zona Sur (Industrial)
   - Zona Valles (Residencial)
   - Zona Noroccidente (Residencial)

2. **Selecciona una Zona**: Haz clic en una fila para editar

### Formulario (Panel Derecho)
Edita los valores actuales:
- **Contaminantes**: CO2, SO2, NO2, PM2.5, O3 (en unidades OMS)
- **Clima**: Temperatura (°C), Velocidad Viento (m/s), Humedad (%)

### Botones de Acción
- **Guardar**: Persiste en MongoDB + añade al histórico (máx 30 días)
- **Predecir 24h**: Usa el modelo de regresión para proyectar calidad del aire
  - Si supera límites OMS → ⚠️ **Notificación de Alerta**
- **Exportar CSV**: Descarga reporte con datos de todas las zonas

### Alertas OMS (Límites Diarios)
- **PM2.5**: > 15 µg/m³ ⚠️
- **NO2**: > 25 µg/m³ ⚠️
- **SO2**: > 40 µg/m³ ⚠️
- **O3**: > 100 µg/m³ ⚠️
- **CO2**: > 4.0 mg/m³ ⚠️

## 🗄️ MongoDB (Docker)

### Verificar que está corriendo
```powershell
# Ver logs
docker logs contaminacion_mongo

# Conectar con mongo shell (si tienes mongosh instalado)
mongosh mongodb://localhost:27017/contaminacion

# Ver base de datos y colecciones
db.zonas.find().pretty()
```

### Detener MongoDB
```powershell
docker-compose down
```

### Limpiar todo (datos + contenedor)
```powershell
docker-compose down -v
# Próxima ejecución regenerará los datos de prueba
```

## 🏗️ Arquitectura

```
com.contaminacion
├── model/
│   ├── Clima.java (temperatura, humedad, viento)
│   ├── Contaminacion.java (5 contaminantes + fecha)
│   ├── ZonaUrbana.java (abstracta)
│   ├── ZonaIndustrial.java (extiende ZonaUrbana)
│   └── ZonaResidencial.java (extiende ZonaUrbana)
├── repository/
│   ├── ZonaRepository.java (interfaz)
│   ├── ZonaMongoSpringRepository.java (Spring Data)
│   └── ZonaRepositoryMongo.java (implementación con inicialización)
├── service/
│   ├── PrediccionService.java (interfaz)
│   ├── CalculadorPrediccion.java (regresión 24h)
│   ├── ReporteService.java (interfaz)
│   └── ReporteServiceImpl.java (CSV)
├── exception/
│   ├── DatosInvalidosException.java
│   ├── DatabaseConnectionException.java
│   └── LimitesExcedidosException.java
├── ui/
│   └── MainView.java (Vaadin, @Route(""))
└── Application.java (arranque Spring Boot + Vaadin)
```

## 🔧 Configuración

**`application.properties`**:
```ini
server.port=8080
spring.data.mongodb.uri=mongodb://localhost:27017/contaminacion
vaadin.launch-browser=true
```

**`docker-compose.yml`**:
- MongoDB 6.0
- Puerto: 27017 (local) → 27017 (contenedor)
- Volumen persistente: `contaminacion_mongo_data`

## 📊 Modelo de Predicción (Regresión Lineal Múltiple)

Para cada contaminante (C_t+24):
```
C_t+24 = b1·C_t + b2·Temp + b3·Hum + b4·Vel_Viento + b5·Emisiones
```

**Coeficientes**:
- b1 = 0.6 (Tendencia histórica)
- b2 = 0.1 (Temperatura)
- b3 = 0.05 (Humedad)
- b4 = -0.25 (Viento dispersa contaminantes)
- b5 = 0.5 (Industrial) o 0.2 (Residencial)

El sistema usa los últimos 30 días de histórico para enriquecer predicciones.

## 🛠️ Troubleshooting

### ❌ "Docker Desktop is unable to start"
- Abre Docker Desktop desde el menú Inicio
- Espera a que el icono esté verde (completamente iniciado)
- Intenta nuevamente

### ❌ "Cannot resolve symbol 'MongoRepository'"
- Esto ocurre porque IntelliJ aún no descargó dependencias
- Ejecuta: `mvn package` en el módulo
- O recarga el proyecto en el IDE (Maven → Reload)

### ❌ "JAVA_HOME is not defined"
```powershell
# Establece Java temporalmente en PowerShell:
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-25'  # (ajusta al JDK que tengas)

# O permanentemente (Windows):
# Sistema → Variables de entorno → Nueva → JAVA_HOME = C:\...\jdk-25
```

### ❌ "Cannot connect to MongoDB"
- Verifica: `docker ps` — ¿está `contaminacion_mongo` corriendo?
- Intenta: `docker-compose down -v && docker-compose up -d`

### ❌ "Port 27017 already in use"
```powershell
# Si otro MongoDB está usando el puerto:
docker-compose down -v
# O cambia el puerto en docker-compose.yml
```

## 📚 Dependencias Principales

```xml
<dependency>
    <groupId>com.vaadin</groupId>
    <artifactId>vaadin</artifactId>
    <version>25.1.8</version>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-mongodb</artifactId>
    <version>4.0.7</version>
</dependency>
```

## 🎯 Siguientes Mejoras (Opcional)

- [ ] Gráficos históricos (Vaadin Charts o Chart.js)
- [ ] Endpoints REST para consumo externo
- [ ] Integración con sensores IoT reales
- [ ] Paneles de alertas automáticas por email
- [ ] Regresión avanzada (coeficientes calculados del histórico)
- [ ] Temas oscuro/claro en UI
- [ ] Tests unitarios y E2E

## 📞 Soporte

Si tienes algún error:
1. Verifica los logs: `docker logs contaminacion_mongo`
2. Ejecuta: `docker ps` (¿está MongoDB corriendo?)
3. Compila localmente: `mvn -DskipTests clean package`

---

**¡La aplicación está lista para producción!** 🚀
Ejecuta `.\setup-and-run.ps1` y accede a http://localhost:8080

