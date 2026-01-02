package software.ulpgc.hospital.query.app.adapters;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import software.ulpgc.hospital.query.domain.control.QueryStatsCommand;
import software.ulpgc.hospital.query.domain.control.Response;
import software.ulpgc.hospital.query.utils.HateoasBuilder;

import java.util.HashMap;

public class QueryStatsAdapter {
    public static QueryStatsCommand.Input adapt(APIGatewayProxyRequestEvent request) {
        return () -> request.getQueryStringParameters() == null ? new HashMap<>() : request.getQueryStringParameters();
    }

    public static QueryStatsCommand.Output adaptedOutput() {
        return result -> Response.with(
                HateoasBuilder.forCollection(result, "/stats")
        );
    }
}
