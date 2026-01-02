# Hospital Event Processing Pipeline (AWS)

Pipeline serverless para ingestar eventos clínicos, almacenarlos en S3 y construir agregaciones en DynamoDB, exponiendo consultas vía API Gateway.

## Arquitectura

- API Gateway expone la REST API y requiere API Key en los métodos configurados.
- Lambda Feeder recibe `POST /events` y publica mensajes en SQS (EventQueue).
- Lambda Ingest consume SQS y guarda eventos en S3 bajo el prefijo `raw/`.
- S3 Trigger (ObjectCreated con filtro `raw/`) invoca Lambda Mounter.
- Lambda Mounter lee de S3 (`raw/`) y escribe agregaciones en DynamoDB Datamart.
- Lambda Query responde consultas leyendo desde DynamoDB o S3 según el endpoint.

## Infraestructura (CloudFormation)

- DynamoDB `*-datamart` (PAY_PER_REQUEST) con PK `pk` y SK `sk`.
- SQS `*-event-queue` con DLQ `*-dlq` y `maxReceiveCount: 3`.
- Lambdas: `*-feeder`, `*-ingest`, `*-mounter`, `*-query`.
- Trigger S3 configurado para `raw/` hacia `*-mounter`.

## Diagrama de Infraestructura
<img src="assets/CN-Optativo.jpg">

## Scripts (PowerShell)
- `variables.ps1`: establece las variables de entorno necesarias utilizadas en los siguientes scripts. 
- `bucket.ps1`: compila con Maven Wrapper, crea el bucket, crea `raw/` y `scripts/`, y sube los JARs a `scripts/`.
- `deploy.ps1`: despliega el stack y configura el trigger S3 → LambdaMounter con prefijo `raw/`; luego imprime el `ApiEndpoint` y el valor de la API key.
- `destroy.ps1`: borra el stack y vacía/elimina el bucket (pide confirmación).

### Requisitos

- AWS CLI configurado (credenciales + región).
- Maven Wrapper `mvnw.cmd` en el root del repo.
- Variables de entorno: establecidas en `variables.ps1` de manera automática.

### Despliegue

Ejecuta `deploy.ps1`. Esto ejecutará primero el script `destroy.ps1` para limpiar cualquier recurso anterior y posteriormente seguirá con `bucket.ps1` para la creación del recurso S3 y la subida de los JARs a `scripts/`. También se ejecutará `variables.ps1` para establecer las variables de entorno necesarias.

## API

Base URL (la imprime `deploy.ps1`): `https://<api-id>.execute-api.<region>.amazonaws.com/prod`.

Endpoints (API Key necesaria en HEADERS con clave `x-apy-key`):
- `POST <base>/events`
- `GET  <base>/events`
- `GET  <base>/stats{?department,date,minAdmissions}`
- `GET  <base>/events-creations/{id}`



## Modelo de dominio

- `Event` define `getStreamId()`, `getTimestamp()` y `getEventType()`.
- `EventType` enumera los tipos de evento.
- `EventCreationStatus` representa el tracking del estado (id/eventType/eventId/stage/timestamps + s3Location/error).
- `Department` enumera los departamentos disponibles.
- `PatientSnapshot` establece información de un paciente en un instante del tiempo (id/patientId/name/surname/age/gender/nationalId).
- `AdmissionEvent` representa un evento de admisión.
- `ConsultationEvent` representa un evento de consulta.

## Troubleshooting

- Si Lambda Mounter no se dispara: verifica que el trigger S3 esté configurado para `s3:ObjectCreated:*` con prefijo `raw/`.
- Si Query devuelve 500: revisa logs de Lambda Query y que las variables de entorno definidas en CloudFormation coincidan con las que lee el código.

## Autor

