<#
  docker-setup.ps1
  Script idempotente para crear/arrancar Mongo, importar seed y construir/arrancar la app

  Uso: ejecutar desde PowerShell (preferiblemente como Administrador) en la raíz del repo:
    powershell -ExecutionPolicy Bypass -File .\docker-setup.ps1

  El script realiza:
  - Comprueba Docker
  - Lanza un contenedor mongo llamado 'mongo-local' si no existe
  - Espera a que Mongo responda y realiza mongoimport de seed/zonas.json
  - Construye la imagen 'contaminacion-app' desde ./contaminacion
  - Lanza el contenedor 'contaminacion_app' y lo enlaza a mongo-local
  - Espera a que la app responda en http://localhost:8080
#>

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Exec([string]$cmd) {
    Write-Host "> $cmd"
    & cmd /c $cmd
}

Write-Host "Iniciando script de configuración Docker para el proyecto 'contaminacion'`n" -ForegroundColor Cyan

# Comprobar Docker
try {
    $dockerVersion = & docker version --format '{{.Server.Version}}' 2>$null
} catch {
    Write-Error "Docker no parece estar disponible. Abre Docker Desktop y asegúrate de que 'Engine running'."
    exit 1
}
Write-Host "Docker disponible (server version: $dockerVersion)" -ForegroundColor Green

$root = Split-Path -Path $MyInvocation.MyCommand.Definition -Parent
Write-Host "Directorio raíz del script: $root"

# Paths
$seedJson = Join-Path $root 'seed\zonas.json'
$appFolder = Join-Path $root 'contaminacion'

if (-Not (Test-Path $seedJson)) {
    Write-Error "No se encontró el seed en $seedJson. Asegúrate de que el archivo exista."
    exit 1
}

# 1) Mongo: crear/arrancar contenedor si no existe
$mongoName = 'mongo-local'
$exists = (& docker ps -a --filter "name=$mongoName" --format "{{.Names}}") -ne ''
if (-not $exists) {
    Write-Host "Creando contenedor Mongo '$mongoName'..."
    Exec "docker run -d --name $mongoName -p 27017:27017 -v contaminacion_mongo_data:/data/db mongo:6.0"
} else {
    $running = (& docker ps --filter "name=$mongoName" --format "{{.Names}}") -ne ''
    if (-not $running) {
        Write-Host "Contenedor $mongoName existe pero no está en ejecución. Iniciando..."
        Exec "docker start $mongoName"
    } else {
        Write-Host "Contenedor $mongoName ya está en ejecución." -ForegroundColor Green
    }
}

# 2) Esperar a que Mongo responda (mongosh/mongo ping)
Write-Host "Esperando a que Mongo responda..."
$max = 60; $i = 0; $ok = $false
while ($i -lt $max) {
    try {
        # Prefer mongosh, si no existe, probar mongo
        & docker exec $mongoName mongosh --eval "db.adminCommand('ping')" > $null 2>&1
        if ($LASTEXITCODE -eq 0) { $ok = $true; break }
    } catch {}
    try {
        & docker exec $mongoName mongo --eval "db.adminCommand('ping')" > $null 2>&1
        if ($LASTEXITCODE -eq 0) { $ok = $true; break }
    } catch {}
    Start-Sleep -Seconds 1
    $i++
}
if (-not $ok) {
    Write-Error "Mongo no respondió después de $max segundos. Revisa 'docker logs $mongoName'"
    Exec "docker logs --tail 200 $mongoName"
    exit 1
}
Write-Host "Mongo está accesible." -ForegroundColor Green

# 3) Copiar seed y ejecutar mongoimport
Write-Host "Copiando seed ($seedJson) al contenedor y ejecutando mongoimport..."
# Copiamos el archivo usando la invocación nativa de PowerShell para evitar problemas de comillas
& docker cp $seedJson "$mongoName:/zonas.json"
try {
    & docker exec -i $mongoName mongoimport --db contaminacion --collection zonas --drop --jsonArray --file /zonas.json
} catch {
    Write-Warn "mongoimport falló. Muestra los logs del contenedor para investigar."
    & docker logs --tail 200 $mongoName
    throw
}
Write-Host "Import realizado (si no hubo errores)." -ForegroundColor Green

# 4) Construir la imagen de la app
if (-Not (Test-Path $appFolder)) {
    Write-Error "No se encontró la carpeta de la app en $appFolder"
    exit 1
}
Write-Host "Construyendo la imagen Docker de la app (esto puede tardar)..."
& docker build -t contaminacion-app $appFolder

# 5) Ejecutar contenedor de la app
$appName = 'contaminacion_app'
$existsApp = (& docker ps -a --filter "name=$appName" --format "{{.Names}}") -ne ''
if ($existsApp) {
    Write-Host "Contenedor $appName ya existe. Eliminando y recreando..."
    Exec "docker rm -f $appName"
}

Write-Host "Arrancando contenedor de la app y conectando a Mongo..."
# Usamos la red por defecto y --link para simplicidad; la app leerá SPRING_DATA_MONGODB_URI
& docker run -d --name $appName --link "$mongoName:mongo" -e "SPRING_DATA_MONGODB_URI=mongodb://mongo:27017/contaminacion" -p 8080:8080 contaminacion-app

# 6) Esperar a que la app responda en http://localhost:8080
Write-Host "Esperando a que la aplicación web responda en http://localhost:8080 ..."
$max = 120; $i = 0; $appOk = $false
while ($i -lt $max) {
    try {
        $r = Invoke-WebRequest -Uri http://localhost:8080 -UseBasicParsing -TimeoutSec 5 -ErrorAction Stop
        if ($r.StatusCode -ge 200) { $appOk = $true; break }
    } catch {}
    Start-Sleep -Seconds 2
    $i++
}
if ($appOk) {
    Write-Host "La aplicación está accesible en http://localhost:8080" -ForegroundColor Green
} else {
    Write-Warn "La aplicación no respondió dentro del tiempo esperado. Revisa los logs del contenedor de la app: docker logs --tail 200 $appName"
    Exec "docker logs --tail 200 $appName"
    exit 1
}

Write-Host "Proceso completado correctamente." -ForegroundColor Cyan
Write-Host "Puedes detener los contenedores con: docker rm -f $mongoName $appName" -ForegroundColor Yellow

