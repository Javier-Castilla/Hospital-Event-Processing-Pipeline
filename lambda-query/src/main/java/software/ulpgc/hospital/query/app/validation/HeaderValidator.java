package software.ulpgc.hospital.query.app.validation;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

import java.util.Map;

public class HeaderValidator extends RequestValidator {
    @Override
    public void validate(APIGatewayProxyRequestEvent request) throws ValidationException {
        Map<String, String> headers = request.getHeaders();
        if (headers == null) {
            throw new ValidationException("Headers cannot be null");
        }
        validateNext(request);
    }
}
