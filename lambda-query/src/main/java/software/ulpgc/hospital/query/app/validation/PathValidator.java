package software.ulpgc.hospital.query.app.validation;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

public class PathValidator extends RequestValidator {
    @Override
    public void validate(APIGatewayProxyRequestEvent request) throws ValidationException {
        String path = request.getPath();
        if (path == null || path.isEmpty()) throw new ValidationException("Path cannot be empty");
        if (!path.startsWith("/events") && !path.startsWith("/stats")) throw new ValidationException("Invalid path: must start with /events");
        validateNext(request);
    }
}
