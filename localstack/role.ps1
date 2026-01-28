# Crear JSONs SIN BOM
$utf8NoBOM = New-Object System.Text.UTF8Encoding $false

$trust = '{"Version":"2012-10-17","Statement":[{"Effect":"Allow","Principal":{"Service":"ec2.amazonaws.com"},"Action":"sts:AssumeRole"}]}'
$perms = '{"Version":"2012-10-17","Statement":[{"Effect":"Allow","Action":"*","Resource":"*"}]}'

[System.IO.File]::WriteAllText("$PWD\trust.json", $trust, $utf8NoBOM)
[System.IO.File]::WriteAllText("$PWD\perms.json", $perms, $utf8NoBOM)

# Crear rol
awsl iam create-role --role-name LabRole --assume-role-policy-document file://trust.json

# Añadir permisos
awsl iam put-role-policy --role-name LabRole --policy-name LabPolicy --policy-document file://perms.json

# Limpiar
Remove-Item trust.json, perms.json

Write-Host "OK - LabRole creado" -ForegroundColor Green
