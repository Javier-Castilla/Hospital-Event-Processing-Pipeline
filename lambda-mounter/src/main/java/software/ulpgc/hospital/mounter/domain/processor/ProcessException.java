package software.ulpgc.hospital.mounter.domain.processor;

public class ProcessException extends Exception {
    public ProcessException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProcessException(String message) {
        super(message);
    }
}
