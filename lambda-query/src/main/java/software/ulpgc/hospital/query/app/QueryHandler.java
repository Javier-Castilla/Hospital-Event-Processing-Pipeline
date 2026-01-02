package software.ulpgc.hospital.query.app;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import software.ulpgc.hospital.domain.stage.repository.EventCreationStatusRepository;
import software.ulpgc.hospital.query.app.adapters.GetEventCreationStatusByIdAdapter;
import software.ulpgc.hospital.query.app.adapters.GetStatsByIdAdapter;
import software.ulpgc.hospital.query.app.adapters.QueryEventsAdapter;
import software.ulpgc.hospital.query.app.adapters.QueryStatsAdapter;
import software.ulpgc.hospital.query.app.config.CommandFactory;
import software.ulpgc.hospital.query.app.config.DependencyFactory;
import software.ulpgc.hospital.query.app.validation.*;
import software.ulpgc.hospital.query.domain.control.*;
import software.ulpgc.hospital.query.domain.repository.DatamartRepository;
import software.ulpgc.hospital.query.domain.repository.EventRepository;

import java.util.HashMap;
import java.util.Map;

public class QueryHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final DatamartRepository datamartRepository;
    private final EventRepository eventRepository;
    private final ObjectMapper objectMapper;
    private final RequestValidator validationChain;
    private final CommandFactory commandFactory;
    private final EventCreationStatusRepository eventCreationStatusRepository;

    public QueryHandler() {
        DependencyFactory factory = DependencyFactory.getInstance();
        this.datamartRepository = factory.getDatamartRepository();
        this.eventRepository = factory.getEventRepository();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.validationChain = PathValidator.withDefaultPaths();
        validationChain.setNext(new MethodValidator())
                .setNext(new HeaderValidator())
                .setNext(new QueryValidator());
        this.commandFactory = createCommandFactory();
        eventCreationStatusRepository = factory.getEventCreationStatusRepository();
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        try {
            validationChain.validate(request);
            String commandKey = request.getHttpMethod() + ":" + request.getResource();
            Response emptyResponse = new Response(200, "OK", null);
            Response response = commandFactory.with(request, emptyResponse).build(commandKey).execute();
            return createResponse(response.code(), response.result());
        } catch (ValidationException e) {
            context.getLogger().log("Validation error: " + e.getMessage());
            return createResponse(400, Map.of("error", e.getMessage()));
        } catch (Exception e) {
            context.getLogger().log("Error: " + e.getMessage());
            return createResponse(500, Map.of("error", "Internal server error"));
        }
    }

    private CommandFactory createCommandFactory() {
        return CommandFactory.create()
                .register("GET:/events", createQueryEventsCommand())
                .register("GET:/stats", createQueryStatsCommand())
                .register("GET:/stats/{id}", createGetStatsByIdCommand())
                .register("GET:/event-creations/{id}", createGetEventCreationStatusByIdCommand());
    }

    private CommandFactory.Builder createGetEventCreationStatusByIdCommand() {
        return (request, response) ->
                new GetEventCreationStatusByIdCommand(
                        GetEventCreationStatusByIdAdapter.adapt(request),
                        GetEventCreationStatusByIdAdapter.adaptedOutput(),
                        eventCreationStatusRepository
                );
    }

    private CommandFactory.Builder createGetStatsByIdCommand() {
        return (request, response) ->
                new GetStatsByIdCommand(
                        GetStatsByIdAdapter.adapt(request),
                        GetStatsByIdAdapter.adaptedOutput(),
                        datamartRepository
                );
    }

    private CommandFactory.Builder createQueryStatsCommand() {
        return (request, response) ->
                new QueryStatsCommand(
                        QueryStatsAdapter.adapt(request),
                        QueryStatsAdapter.adaptedOutput(),
                        datamartRepository
                );
    }

    private CommandFactory.Builder createQueryEventsCommand() {
        return (request, response) ->
                new QueryEventsCommand(
                        QueryEventsAdapter.adapt(request),
                        QueryEventsAdapter.adaptedOutput(),
                        eventRepository
                );
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
