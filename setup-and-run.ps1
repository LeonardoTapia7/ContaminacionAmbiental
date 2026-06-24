# ============================================================================
# Script de Setup y Ejecucion: MongoDB + Aplicacion Contaminacion
# ============================================================================
# Este script hace todo lo necesario para ejecutar la aplicacion:
# 1. Verifica que Docker Desktop este corriendo
# 2. Levanta MongoDB con docker-compose
# 3. Espera a que MongoDB este listo
# 4. Descarga dependencias Maven
# 5. Compila la aplicacion
# 6. Ejecuta la aplicacion (http://localhost:8080)
# ============================================================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "SETUP: Sistema de Contaminacion + MongoDB" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Variables
$projectRoot = Split-Path -Parent $MyInvocation.MyCommandPath
$projectModule = Join-Path $projectRoot "contaminacion"
$dockerComposeFile = Join-Path $projectRoot "docker-compose.yml"

# Colores
$successColor = "Green"
$errorColor = "Red"
$warningColor = "Yellow"
$infoColor = "Cyan"

# Funcion helper
function Write-Status {
    param([string]$Message, [string]$Type = "info")
    $color = switch($Type) {
        "success" { $successColor }
        "error" { $errorColor }
        "warning" { $warningColor }
        default { $infoColor }
    }
    Write-Host "[$([DateTime]::Now.ToString('HH:mm:ss'))] $Message" -ForegroundColor $color
}

# Paso 1: Verificar Docker
Write-Status "Verificando Docker..." "info"
$dockerCheck = docker --version 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Status "ERROR: Docker no esta instalado o no esta en PATH" "error"
    Write-Host "Descarga Docker Desktop desde: https://www.docker.com/products/docker-desktop" -ForegroundColor $warningColor
    exit 1
}
Write-Status "Docker detectado: $dockerCheck" "success"

# Paso 2: Verificar que Docker daemon esta corriendo
Write-Status "Verificando que Docker daemon esta activo..." "info"
$dockerPs = docker ps 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Status "ERROR: Docker daemon no esta corriendo. Por favor abre Docker Desktop." "error"
    exit 1
}
Write-Status "Docker daemon esta activo" "success"

# Paso 3: Levantar MongoDB
Write-Status "Levantando MongoDB con docker-compose..." "info"
Push-Location $projectRoot
docker-compose down 2>&1 | Out-Null
docker-compose up -d 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Status "ERROR: No se pudo levantar MongoDB" "error"
    Pop-Location
    exit 1
}
Pop-Location
Write-Status "MongoDB levantado exitosamente" "success"

# Paso 4: Esperar a que MongoDB este listo
Write-Status "Esperando a que MongoDB este listo..." "info"
$mongoReady = $false
$retries = 0
$maxRetries = 30
while (-not $mongoReady -and $retries -lt $maxRetries) {
    $test = docker exec contaminacion_mongo mongo --version 2>&1
    if ($LASTEXITCODE -eq 0) {
        $mongoReady = $true
    } else {
        Start-Sleep -Seconds 1
        $retries++
        Write-Host "." -NoNewline -ForegroundColor Cyan
    }
}
Write-Host ""
if ($mongoReady) {
    Write-Status "MongoDB esta listo" "success"
} else {
    Write-Status "WARNING: MongoDB puede no estar completamente listo, pero continuando..." "warning"
}

# Paso 5: Descargar dependencias y compilar
Write-Status "Descargando dependencias Maven y compilando..." "info"
Push-Location $projectModule
& .\mvnw.cmd -DskipTests clean install 2>&1 | Tee-Object -Variable mvnOutput | Out-Null
if ($LASTEXITCODE -ne 0) {
    Write-Status "ERROR: La compilacion fallo. Output:" "error"
    Write-Host $mvnOutput -ForegroundColor $errorColor
    Pop-Location
    exit 1
}
Write-Status "Compilacion exitosa" "success"

# Paso 6: Ejecutar la aplicacion
Write-Status "==========================================" "info"
Write-Status "INICIANDO APLICACION" "success"
Write-Status "==========================================" "info"
Write-Status "La aplicacion estara disponible en: http://localhost:8080" "success"
Write-Status "MongoDB conectado a: mongodb://localhost:27017/contaminacion" "success"
Write-Status "Presiona CTRL+C para detener la aplicacion" "warning"
Write-Status "==========================================" "info"

& .\mvnw.cmd -DskipTests spring-boot:run
Pop-Location

