# PowerShell script to start all 4 backend services natively (since Docker is not installed)

Write-Host "--- Checking Environment ---" -ForegroundColor Cyan
java -version
mvn -version

Write-Host "`n--- Preparing Temporary Directory on E: ---" -ForegroundColor Cyan
if (!(Test-Path "E:\temp")) { New-Item -ItemType Directory -Path "E:\temp" }

Write-Host "`n--- Compiling Modules ---" -ForegroundColor Cyan
mvn compile -DskipTests -s settings-native.xml "-Djava.io.tmpdir=E:\temp"

if ($LASTEXITCODE -ne 0) {
    Write-Host "Build failed! Please check the errors above." -ForegroundColor Red
    exit $LASTEXITCODE
}

Write-Host "`n--- Starting Microservices (Native) ---" -ForegroundColor Cyan
$MEM_OPTS = "-Xmx256m -Djava.io.tmpdir=E:\temp"

# 1. Eureka Server (Wait 15s)
Write-Host "1/4 Starting Eureka Server (Port 8761)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd eureka-server; `$env:MAVEN_OPTS='$MEM_OPTS'; mvn spring-boot:run -s ../settings-native.xml"
Start-Sleep -Seconds 15

# 2. Auth Service
Write-Host "2/4 Starting Auth Service (Port 8082)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd auth-service; `$env:MAVEN_OPTS='$MEM_OPTS'; mvn spring-boot:run '-Dspring-boot.run.profiles=dev' -s ../settings-native.xml"

# 3. Product-Order-Payment Service
Write-Host "3/4 Starting Order-Payment Service (Port 8081)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd product-order-payment; `$env:MAVEN_OPTS='$MEM_OPTS'; mvn spring-boot:run '-Dspring-boot.run.profiles=dev' -s ../settings-native.xml"

# 4. API Gateway
Write-Host "4/4 Starting API Gateway (Port 8080)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd api-gateway; `$env:MAVEN_OPTS='$MEM_OPTS'; mvn spring-boot:run -s ../settings-native.xml"

Write-Host "`nAll services have been launched in separate windows." -ForegroundColor Green
Write-Host "Check http://localhost:8761 for Eureka Status." -ForegroundColor Green
Write-Host "Check http://localhost:8080/swagger-ui.html for API Docs." -ForegroundColor Green
