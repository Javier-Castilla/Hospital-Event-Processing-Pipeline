package software.ulpgc.hospital.feeder.app;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.ulpgc.hospital.feeder.app.config.DependencyFactory;
import software.ulpgc.hospital.feeder.app.config.ValidationFactory;
import software.ulpgc.hospital.feeder.app.docs.RecordSchema;
import software.ulpgc.hospital.feeder.domain.publisher.MessagePublisher;
import software.ulpgc.hospital.feeder.domain.publisher.PublishResult;
import software.ulpgc.hospital.feeder.domain.validation.ValidationResult;
import software.ulpgc.hospital.model.AdmissionEvent;
import software.ulpgc.hospital.model.ConsultationEvent;
import software.ulpgc.hospital.model.Event;
import software.ulpgc.hospital.model.EventType;
import software.ulpgc.hospital.model.serialization.EventDeserializer;
import software.ulpgc.hospital.model.serialization.EventSerializer;
import software.ulpgc.hospital.model.serialization.SerializationException;

import java.util.HashMap;
import java.util.Map;

public class FeederHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final EventDeserializer deserializer;
    private final ValidationFactory validationFactory;
    private final EventSerializer serializer;
    private final MessagePublisher publisher;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public FeederHandler() {
        DependencyFactory factory = DependencyFactory.getInstance();
        this.deserializer = factory.getEventDeserializer();
        this.validationFactory = factory.getValidationFactory();
        this.serializer = factory.getEventSerializer();
        this.publisher = factory.getMessagePublisher();
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        context.getLogger().log("Received event: " + input.getBody());

        try {
            Event event = deserializer.deserialize(input.getBody());

            ValidationResult validationResult = validationFactory.validate(event);
            if (!validationResult.isValid()) {
                return createValidationErrorResponse(400, validationResult.getErrorMessage(), event, context);
            }

            String eventJson = serializer.serialize(event);
            PublishResult publishResult = publisher.publish(eventJson);
            return createSuccessResponse(publishResult.getMessageId());

        } catch (SerializationException e) {
            context.getLogger().log("Deserialization error: " + e.getMessage());
            return createDeserializationErrorResponse(400, e.getMessage(), input.getBody(), context);

        } catch (Exception e) {
            context.getLogger().log("Error processing event: " + e.getMessage());
            return createInternalErrorResponse(500, context);
        }
    }

    private APIGatewayProxyResponseEvent createSuccessResponse(String messageId) {
        Map<String, String> headers = defaultHeaders("application/json");

        Map<String, Object> body = Map.of(
                "message", "Event published successfully",
                "messageId", messageId
        );

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withHeaders(headers)
                .withBody(toJsonSafe(body));
    }

    private APIGatewayProxyResponseEvent createValidationErrorResponse(int statusCode, String message, Event event, Context context) {
        Map<String, String> headers = defaultHeaders("application/problem+json");

        Map<String, Object> body = new HashMap<>();
        body.put("type", "about:blank");
        body.put("title", "Validation error");
        body.put("status", statusCode);
        body.put("detail", safeClientDetail(message));
        body.put("instance", "urn:aws:requestId:" + context.getAwsRequestId());
        body.put("eventType", event.getEventType().name());
        body.put("expected", RecordSchema.of(event.getClass()));

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withHeaders(headers)
                .withBody(toJsonSafe(body));
    }

    private APIGatewayProxyResponseEvent createDeserializationErrorResponse(int statusCode, String technicalMessage, String rawBody, Context context) {
        Map<String, String> headers = defaultHeaders("application/problem+json");

        EventType eventType = tryExtractEventType(rawBody);
        Class<?> clazz = classFor(eventType);

        Map<String, Object> body = new HashMap<>();
        body.put("type", "about:blank");
        body.put("title", "Invalid request body");
        body.put("status", statusCode);
        body.put("detail", isDebugEnabled() ? technicalMessage : "Invalid request body (check 'expected').");
        body.put("instance", "urn:aws:requestId:" + context.getAwsRequestId());
        body.put("eventType", eventType == null ? null : eventType.name());

        if (clazz == null) {
            body.put("expected", Map.of(
                    "required", new String[]{"eventType"},
                    "supportedEventTypes", EventType.values()
            ));
        } else {
            body.put("expected", RecordSchema.of(clazz));
        }

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withHeaders(headers)
                .withBody(toJsonSafe(body));
    }

    private APIGatewayProxyResponseEvent createInternalErrorResponse(int statusCode, Context context) {
        Map<String, String> headers = defaultHeaders("application/problem+json");

        Map<String, Object> body = new HashMap<>();
        body.put("type", "about:blank");
        body.put("title", "Internal Server Error");
        body.put("status", statusCode);
        body.put("detail", "Internal server error");
        body.put("instance", "urn:aws:requestId:" + context.getAwsRequestId());

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withHeaders(headers)
                .withBody(toJsonSafe(body));
    }

    private Map<String, String> defaultHeaders(String contentType) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", contentType);
        headers.put("Access-Control-Allow-Origin", "*");
        return headers;
    }

    private String toJsonSafe(Object body) {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            return "{\"error\":\"Failed to serialize response\"}";
        }
    }

    private EventType tryExtractEventType(String rawBody) {
        try {
            JsonNode node = objectMapper.readTree(rawBody);
            JsonNode typeNode = node.get("eventType");
            if (typeNode == null || typeNode.isNull()) return null;
            return EventType.valueOf(typeNode.asText());
        } catch (Exception e) {
            return null;
        }
    }

    private Class<?> classFor(EventType type) {
        if (type == null) return null;
        return switch (type) {
            case ADMISSION -> AdmissionEvent.class;
            case CONSULTATION -> ConsultationEvent.class;
            default -> null;
        };
    }

    private boolean isDebugEnabled() {
        return "true".equalsIgnoreCase(System.getenv("DEBUG"));
    }

    private String safeClientDetail(String message) {
        if (message == null) return "Invalid request.";
        if (isDebugEnabled()) return message;
        return "Validation failed (check 'expected').";
    }
}
