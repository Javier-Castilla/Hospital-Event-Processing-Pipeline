package software.ulpgc.hospital.query.app.adapters;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import software.ulpgc.hospital.query.domain.control.GetEventCreationStatusByIdCommand;
import software.ulpgc.hospital.query.domain.control.Response;
import software.ulpgc.hospital.query.utils.HateoasBuilder;

import java.util.UUID;

public class GetEventCreationStatusByIdAdapter {
    public static GetEventCreationStatusByIdCommand.Input adapt(APIGatewayProxyRequestEvent request) {
        return () -> UUID.fromString(request.getPathParameters().get("id"));
    }

    public static GetEventCreationStatusByIdCommand.Output adaptedOutput() {
        return result -> Response.with(
                HateoasBuilder.forSingleResource(result, "/event-creations/" + result.id(), "/event-creation")
        );
    }
}
