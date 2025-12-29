package software.ulpgc.hospital.query.domain.query;

public class QueryException extends Exception {
    public QueryException(String message, Throwable cause) {
        super(message, cause);
    }

    public QueryException(String message) {
        super(message);
    }
}
