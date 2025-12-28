package software.ulpgc.hospital.feeder.publisher;

public class PublishException extends Exception {
    public PublishException(String message, Throwable cause) {
        super(message, cause);
    }

    public PublishException(String message) {
        super(message);
    }
}
