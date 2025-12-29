$ErrorActionPreference = "Continue"

. .\variables.ps1

Write-Host "======================================" -ForegroundColor Cyan
Write-Host "Destroying Hospital Pipeline" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan

Write-Host ""
Write-Host "WARNING: This will delete all resources!" -ForegroundColor Red
$confirm = Read-Host "Are you sure? Type 'yes' to continue"

if ($confirm -ne "yes") {
    Write-Host "Cancelled" -ForegroundColor Yellow
    exit 0
}

Write-Host ""
Write-Host "Step 1: Deleting CloudFormation stack..." -ForegroundColor Yellow
$stackExists = aws cloudformation describe-stacks --stack-name $env:STACK_NAME --region $env:AWS_REGION 2>$null

if ($LASTEXITCODE -eq 0) {
    aws cloudformation delete-stack --stack-name $env:STACK_NAME --region $env:AWS_REGION
    Write-Host "Waiting for stack deletion..." -ForegroundColor Yellow
    aws cloudformation wait stack-delete-complete --stack-name $env:STACK_NAME --region $env:AWS_REGION
    Write-Host "Stack deleted successfully" -ForegroundColor Green
} else {
    Write-Host "Stack does not exist (nothing to delete)" -ForegroundColor Gray
}

Write-Host ""
Write-Host "Step 2: Emptying and deleting DataLake bucket..." -ForegroundColor Yellow
$bucketExists = aws s3 ls "s3://$env:BUCKET_NAME" 2>$null

if ($LASTEXITCODE -eq 0) {
    Write-Host "Emptying bucket..." -ForegroundColor Yellow
    aws s3 rm "s3://$env:BUCKET_NAME" --recursive 2>$null | Out-Null

    Write-Host "Deleting bucket..." -ForegroundColor Yellow
    aws s3 rb "s3://$env:BUCKET_NAME" 2>$null

    if ($LASTEXITCODE -eq 0) {
        Write-Host "Bucket deleted: $env:BUCKET_NAME" -ForegroundColor Green
    } else {
        Write-Host "Could not delete bucket (may not be empty)" -ForegroundColor Yellow
    }
} else {
    Write-Host "Bucket does not exist (nothing to delete)" -ForegroundColor Gray
}

Write-Host ""
Write-Host "======================================" -ForegroundColor Cyan
Write-Host "Cleanup complete!" -ForegroundColor Green
Write-Host "======================================" -ForegroundColor Cyan
