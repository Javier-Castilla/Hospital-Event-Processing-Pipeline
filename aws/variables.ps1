$env:STACK_NAME="hospital-events-stack"
$env:AWS_REGION="us-east-1"
$env:ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
$env:BUCKET_NAME="datalake-hospital-events-$env:ACCOUNT_ID"
$env:ROLE_ARN=$(aws iam get-role --role-name LabRole --query 'Role.Arn' --output text)
