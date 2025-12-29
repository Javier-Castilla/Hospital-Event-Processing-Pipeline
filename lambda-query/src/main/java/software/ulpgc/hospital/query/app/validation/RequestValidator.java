package software.ulpgc.hospital.query.app.validation;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

public abstract class RequestValidator {
    protected RequestValidator next;

    public RequestValidator setNext(RequestValidator next) {
        this.next = next;
        return next;
    }

    public abstract void validate(APIGatewayProxyRequestEvent request) throws ValidationException;

    protected void validateNext(APIGatewayProxyRequestEvent request) throws ValidationException {
        if (next != null) {
            next.validate(request);
        }
    }
}
