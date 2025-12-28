package software.ulpgc.hospital.feeder.app;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import software.ulpgc.hospital.feeder.app.config.DependencyFactory;
import software.ulpgc.hospital.feeder.domain.publisher.MessagePublisher;
import software.ulpgc.hospital.feeder.domain.publisher.PublishResult;
import software.ulpgc.hospital.feeder.domain.validator.EventValidator;
import software.ulpgc.hospital.feeder.domain.validator.ValidationResult;
import software.ulpgc.hospital.model.Event;
import software.ulpgc.hospital.model.serialization.EventDeserializer;
import software.ulpgc.hospital.model.serialization.EventSerializer;

import java.util.HashMap;
import java.util.Map;

public class FeederHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final EventDeserializer<Event> deserializer;
    private final EventValidator validator;
    private final EventSerializer serializer;
    private final MessagePublisher publisher;

    public FeederHandler() {
        DependencyFactory factory = DependencyFactory.getInstance();
        this.deserializer = factory.getEventDeserializer();
        this.validator = factory.getEventValidator();
        this.serializer = factory.getEventSerializer();
        this.publisher = factory.getMessagePublisher();
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        context.getLogger().log("Received event: " + input.getBody());

        try {
            // 1. Deserializar evento
            Event event = deserializer.deserialize(input.getBody());

            // 2. Validar evento
            ValidationResult validationResult = validator.validate(event);
            if (!validationResult.isValid()) {
                return createErrorResponse(400, validationResult.getErrorMessage());
            }

            // 3. Serializar evento
            String eventJson = serializer.serialize(event);

            // 4. Publicar a SQS
            PublishResult publishResult = publisher.publish(eventJson);

            // 5. Responder éxito
            return createSuccessResponse(publishResult.getMessageId());

        } catch (Exception e) {
            context.getLogger().log("Error processing event: " + e.getMessage());
            return createErrorResponse(500, "Internal server error: " + e.getMessage());
        }
    }

    private APIGatewayProxyResponseEvent createSuccessResponse(String messageId) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Access-Control-Allow-Origin", "*");

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withHeaders(headers)
                .withBody("{\"message\":\"Event published successfully\",\"messageId\":\"" + messageId + "\"}");
    }

    private APIGatewayProxyResponseEvent createErrorResponse(int statusCode, String message) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Access-Control-Allow-Origin", "*");

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withHeaders(headers)
                .withBody("{\"error\":\"" + message + "\"}");
    }
}
