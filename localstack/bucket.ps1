$ErrorActionPreference = "Stop"

. .\variables.ps1

Write-Host "======================================" -ForegroundColor Cyan
Write-Host "DataLake Setup & Code Upload" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan

Write-Host ""
Write-Host "Step 1: Building Lambda functions..." -ForegroundColor Green

Push-Location ..
try {
    if (Test-Path ".\mvnw.cmd") {
        & .\mvnw.cmd clean package -DskipTests
    } else {
        Write-Host "ERROR: Maven Wrapper not found!" -ForegroundColor Red
        Pop-Location
        exit 1
    }

    if ($LASTEXITCODE -ne 0) {
        Write-Host "Maven build failed!" -ForegroundColor Red
        Pop-Location
        exit 1
    }
} finally {
    Pop-Location
}

Write-Host "Build successful!" -ForegroundColor Green

Write-Host ""
Write-Host "Step 2: Creating DataLake S3 bucket..." -ForegroundColor Green
Write-Host "Bucket name: $env:BUCKET_NAME" -ForegroundColor Gray

awsl s3 mb "s3://$env:BUCKET_NAME" --region $env:AWS_REGION 2>$null
if ($LASTEXITCODE -eq 0) {
    Write-Host "Bucket created: $env:BUCKET_NAME" -ForegroundColor Green
} else {
    Write-Host "Bucket already exists or created: $env:BUCKET_NAME" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Step 3: Creating folder structure..." -ForegroundColor Green
$emptyFile = New-TemporaryFile
"" | Out-File -FilePath $emptyFile.FullName -Encoding ASCII

awsl s3api put-object --bucket $env:BUCKET_NAME --key raw/
awsl s3api put-object --bucket $env:BUCKET_NAME --key scripts/

Remove-Item $emptyFile.FullName
Write-Host "Folder structure created" -ForegroundColor Green

Write-Host ""
Write-Host "Step 4: Uploading Lambda JARs to scripts/ folder..." -ForegroundColor Green

awsl s3 cp ../lambda-feeder/target/lambda-feeder-1.0.0.jar "s3://$env:BUCKET_NAME/scripts/lambda-feeder.jar" | Out-Null
Write-Host "  lambda-feeder.jar uploaded" -ForegroundColor Gray

awsl s3 cp ../lambda-ingest/target/lambda-ingest-1.0.0.jar "s3://$env:BUCKET_NAME/scripts/lambda-ingest.jar" | Out-Null
Write-Host "  lambda-ingest.jar uploaded" -ForegroundColor Gray

awsl s3 cp ../lambda-mounter/target/lambda-mounter-1.0.0.jar "s3://$env:BUCKET_NAME/scripts/lambda-mounter.jar" | Out-Null
Write-Host "  lambda-mounter.jar uploaded" -ForegroundColor Gray

awsl s3 cp ../lambda-query/target/lambda-query-1.0.0.jar "s3://$env:BUCKET_NAME/scripts/lambda-query.jar" | Out-Null
Write-Host "  lambda-query.jar uploaded" -ForegroundColor Gray

Write-Host ""
Write-Host "======================================" -ForegroundColor Cyan
Write-Host "DataLake Setup Complete!" -ForegroundColor Green
Write-Host "======================================" -ForegroundColor Cyan

Write-Host ""
Write-Host "DataLake Structure:" -ForegroundColor Cyan
Write-Host "s3://$env:BUCKET_NAME/" -ForegroundColor White
Write-Host "  scripts/" -ForegroundColor Gray
Write-Host "    lambda-feeder.jar" -ForegroundColor Gray
Write-Host "    lambda-ingest.jar" -ForegroundColor Gray
Write-Host "    lambda-mounter.jar" -ForegroundColor Gray
Write-Host "    lambda-query.jar" -ForegroundColor Gray
Write-Host "  raw/ (empty - will be populated by lambda-ingest)" -ForegroundColor Gray
Write-Host "  processed/ (empty - reserved for future use)" -ForegroundColor Gray

Write-Host ""
Write-Host "Next step: Run .\deploy.ps1 to create the infrastructure" -ForegroundColor Yellow
