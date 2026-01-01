package software.ulpgc.hospital.feeder.domain.validator;

public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
