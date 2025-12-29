package software.ulpgc.hospital.query.app.validation;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

public class MethodValidator extends RequestValidator {
    @Override
    public void validate(APIGatewayProxyRequestEvent request) throws ValidationException {
        String method = request.getHttpMethod();
        if (method == null || !method.equals("GET")) throw new ValidationException("Only GET method is allowed");
        validateNext(request);
    }
}
