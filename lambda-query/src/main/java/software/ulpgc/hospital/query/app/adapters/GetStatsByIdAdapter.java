package software.ulpgc.hospital.query.app.adapters;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import software.ulpgc.hospital.query.domain.control.GetStatsByIdCommand;
import software.ulpgc.hospital.query.domain.control.Response;
import software.ulpgc.hospital.query.utils.HateoasBuilder;

public class GetStatsByIdAdapter {
    public static GetStatsByIdCommand.Input adapt(APIGatewayProxyRequestEvent request) {
        return () -> request.getPathParameters().get("id");
    }

    public static GetStatsByIdCommand.Output adaptedOutput() {
        return result -> Response.with(
                HateoasBuilder.forSingleResource(result, "/stats/" + result.getPartitionKey(), "/stats")
        );
    }

}
