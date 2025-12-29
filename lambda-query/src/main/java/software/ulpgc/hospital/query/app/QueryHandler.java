package software.ulpgc.hospital.query.app;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.ulpgc.hospital.query.app.config.DependencyFactory;
import software.ulpgc.hospital.query.app.validation.*;
import software.ulpgc.hospital.query.domain.query.QueryResult;
import software.ulpgc.hospital.query.domain.query.QueryService;

import java.util.HashMap;
import java.util.Map;

public class QueryHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final QueryService queryService;
    private final ObjectMapper objectMapper;
    private final RequestValidator validationChain;

    public QueryHandler() {
        DependencyFactory factory = DependencyFactory.getInstance();
        this.queryService = factory.getQueryService();
        this.objectMapper = new ObjectMapper();
        this.validationChain = new PathValidator();
        validationChain.setNext(new MethodValidator())
                .setNext(new HeaderValidator());
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        context.getLogger().log("Processing API Gateway request: " + request.getPath());
        try {
            validationChain.validate(request);
            String path = request.getPath();
            QueryResult result;
            if (path.startsWith("/events/")) {
                String eventId = path.substring("/events/".length());
                result = queryService.getEventById(eventId);
            } else if (path.equals("/events")) {
                result = queryService.getAllEvents();
            } else {
                return createResponse(404, Map.of("error", "Not found"));
            }
            if (result.success()) {
                return createResponse(200, result);
            } else {
                return createResponse(404, result);
            }
        } catch (ValidationException e) {
            context.getLogger().log("Validation error: " + e.getMessage());
            return createResponse(400, Map.of("error", e.getMessage()));
        } catch (Exception e) {
            context.getLogger().log("Error processing request: " + e.getMessage());
            return createResponse(500, Map.of("error", "Internal server error"));
        }
    }

    private APIGatewayProxyResponseEvent createResponse(int statusCode, Object body) {
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Access-Control-Allow-Origin", "*");

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(statusCode)
                    .withHeaders(headers)
                    .withBody(objectMapper.writeValueAsString(body));
        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withBody("{\"error\": \"Failed to serialize response\"}");
        }
    }
}
