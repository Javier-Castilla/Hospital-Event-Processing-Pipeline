package software.ulpgc.hospital.query.app.adapters;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import software.ulpgc.hospital.query.domain.control.GetStatsByIdCommand;
import software.ulpgc.hospital.query.domain.control.Response;

public class QueryStatsAdapter {
    public static GetStatsByIdCommand.Input adapt(APIGatewayProxyRequestEvent request) {
        return () -> request.getPathParameters().get("id");
    }

    public static GetStatsByIdCommand.Output adapt(Response response) {
        return Response::with;
    }
}
