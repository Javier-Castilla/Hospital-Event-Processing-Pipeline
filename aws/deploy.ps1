. .\variables.ps1

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Hospital Event Pipeline - Full Deploy" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan

Write-Host ""
Write-Host "Step 1: Destroying existing infrastructure..." -ForegroundColor Yellow
& .\destroy.ps1

Write-Host ""
Write-Host "Step 2: Setting up bucket and uploading code..." -ForegroundColor Yellow
& .\bucket.ps1

Write-Host ""
Write-Host "Step 3: Deploying CloudFormation stack..." -ForegroundColor Yellow

try {
    aws cloudformation create-stack `
        --stack-name $env:STACK_NAME `
        --template-body file://infrastructure.yml `
        --parameters ParameterKey=DataLakeBucketName,ParameterValue=$env:BUCKET_NAME `
        --region $env:AWS_REGION `
        --capabilities CAPABILITY_NAMED_IAM

    Write-Host "Waiting for stack creation..." -ForegroundColor Yellow
    aws cloudformation wait stack-create-complete `
        --stack-name $env:STACK_NAME `
        --region $env:AWS_REGION

    Write-Host "Stack created successfully!" -ForegroundColor Green

    $outputs = aws cloudformation describe-stacks `
        --stack-name $env:STACK_NAME `
        --region $env:AWS_REGION `
        --query 'Stacks[0].Outputs' `
        --output json | ConvertFrom-Json

    Write-Host ""
    Write-Host "Step 4: Configuring S3 Trigger..." -ForegroundColor Yellow

    $mounterName = ($outputs | Where-Object { $_.OutputKey -eq "LambdaMounterName" }).OutputValue
    $accountId = aws sts get-caller-identity --query Account --output text
    $mounterArn = "arn:aws:lambda:${env:AWS_REGION}:${accountId}:function:$mounterName"

    $config = @"
{
    "LambdaFunctionConfigurations": [
        {
            "LambdaFunctionArn": "$mounterArn",
            "Events": ["s3:ObjectCreated:*"],
            "Filter": {
                "Key": {
                    "FilterRules": [
                        {
                            "Name": "prefix",
                            "Value": "raw/"
                        }
                    ]
                }
            }
        }
    ]
}
"@

    [System.IO.File]::WriteAllLines("$PWD\notification-config.json", $config)

    aws s3api put-bucket-notification-configuration `
        --bucket $env:BUCKET_NAME `
        --region $env:AWS_REGION `
        --notification-configuration file://notification-config.json

    Remove-Item notification-config.json

    Write-Host "S3 trigger configured!" -ForegroundColor Green

    Write-Host ""
    Write-Host "=====================================" -ForegroundColor Cyan
    Write-Host "API Information" -ForegroundColor Cyan
    Write-Host "=====================================" -ForegroundColor Cyan

    $apiEndpoint = ($outputs | Where-Object { $_.OutputKey -eq "ApiEndpoint" }).OutputValue
    $apiKeyId = ($outputs | Where-Object { $_.OutputKey -eq "ApiKeyId" }).OutputValue
    $apiKeyValue = aws apigateway get-api-key `
        --api-key $apiKeyId `
        --include-value `
        --query 'value' `
        --output text `
        --region $env:AWS_REGION

    Write-Host "Base URL: " -NoNewline -ForegroundColor Yellow
    Write-Host "$apiEndpoint" -ForegroundColor Cyan
    Write-Host "API Key: " -NoNewline -ForegroundColor Yellow
    Write-Host "$apiKeyValue" -ForegroundColor Green

    Write-Host ""
    Write-Host "=====================================" -ForegroundColor Cyan
    Write-Host "Available Endpoints" -ForegroundColor Cyan
    Write-Host "=====================================" -ForegroundColor Cyan
    Write-Host "POST $apiEndpoint/events" -ForegroundColor White
    Write-Host "GET  $apiEndpoint/events?eventType=ADMISSION&date=2025-12-29" -ForegroundColor White
    Write-Host "GET  $apiEndpoint/stats?department=EMERGENCY" -ForegroundColor White

} catch {
    Write-Host "Deployment failed!" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "=====================================" -ForegroundColor Green
Write-Host "Deployment Completed!" -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Green
