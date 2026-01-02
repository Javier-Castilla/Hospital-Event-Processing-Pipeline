package software.ulpgc.hospital.query.app.adapters;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import software.ulpgc.hospital.query.domain.control.QueryEventsCommand;
import software.ulpgc.hospital.query.domain.control.Response;
import software.ulpgc.hospital.query.utils.HateoasBuilder;

import java.util.HashMap;

public class QueryEventsAdapter {
    public static QueryEventsCommand.Input adapt(APIGatewayProxyRequestEvent request) {
        return () -> request.getQueryStringParameters() == null ? new HashMap<String, String>() : request.getQueryStringParameters();
    }

    public static QueryEventsCommand.Output adaptedOutput() {
        return result -> Response.with(
                HateoasBuilder.forCollection(result, "/events")
        );
    }
}
