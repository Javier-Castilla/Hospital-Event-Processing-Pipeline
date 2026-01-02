package software.ulpgc.hospital.query.domain.control;

public record Response(int code, String message, Object result) {
    public static Response with(Object result) {
        return new Response(200, "OK", result);
    }
}
