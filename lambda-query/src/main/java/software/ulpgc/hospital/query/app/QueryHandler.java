package software.ulpgc.hospital.query.app;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.ulpgc.hospital.model.DepartmentStats;
import software.ulpgc.hospital.model.Event;
import software.ulpgc.hospital.query.app.config.DependencyFactory;
import software.ulpgc.hospital.query.app.validation.HeaderValidator;
import software.ulpgc.hospital.query.app.validation.MethodValidator;
import software.ulpgc.hospital.query.app.validation.PathValidator;
import software.ulpgc.hospital.query.app.validation.RequestValidator;
import software.ulpgc.hospital.query.app.validation.ValidationException;
import software.ulpgc.hospital.query.domain.repository.DatamartRepository;
import software.ulpgc.hospital.query.domain.repository.EventRepository;
import software.ulpgc.hospital.query.domain.repository.RepositoryException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final DatamartRepository datamartRepository;
    private final EventRepository eventRepository;
    private final ObjectMapper objectMapper;
    private final RequestValidator validationChain;

    public QueryHandler() {
        DependencyFactory factory = DependencyFactory.getInstance();
        this.datamartRepository = factory.getDatamartRepository();
        this.eventRepository = factory.getEventRepository();
        this.objectMapper = new ObjectMapper();
        this.validationChain = new PathValidator();
        validationChain.setNext(new MethodValidator())
                .setNext(new HeaderValidator());
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        context.getLogger().log("Processing query: " + request.getPath());
        try {
            validationChain.validate(request);
            String path = request.getPath();

            if (path.startsWith("/stats/")) {
                String id = path.substring("/stats/".length());
                DepartmentStats stats = datamartRepository.findById(id);
                return createResponse(200, stats);

            } else if (path.equals("/stats")) {
                Map<String, String> filters = request.getQueryStringParameters();
                if (filters == null) {
                    filters = new HashMap<>();
                }

                List<DepartmentStats> results = datamartRepository.query(filters);
                return createResponse(200, Map.of("data", results, "count", results.size()));

            } else if (path.equals("/events")) {
                Map<String, String> filters = request.getQueryStringParameters();
                if (filters == null) {
                    filters = new HashMap<>();
                }

                List<Event> events = eventRepository.query(filters);
                return createResponse(200, Map.of("events", events, "count", events.size()));

            } else {
                return createResponse(404, Map.of("error", "Not found"));
            }

        } catch (ValidationException e) {
            context.getLogger().log("Validation error: " + e.getMessage());
            return createResponse(400, Map.of("error", e.getMessage()));
        } catch (RepositoryException e) {
            context.getLogger().log("Repository error: " + e.getMessage());
            return createResponse(404, Map.of("error", e.getMessage()));
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
