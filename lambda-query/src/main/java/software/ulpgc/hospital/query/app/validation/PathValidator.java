package software.ulpgc.hospital.query.app.validation;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

import java.util.Set;

public class PathValidator extends RequestValidator {
    private final Set<String> allowedPrefixes;

    public PathValidator(Set<String> allowedPrefixes) {
        this.allowedPrefixes = Set.copyOf(allowedPrefixes);
    }

    public static PathValidator withDefaultPaths() {
        return new PathValidator(Set.of("/events", "/stats", "/event-creations"));
    }

    @Override
    public void validate(APIGatewayProxyRequestEvent request) throws ValidationException {
        String path = request.getPath();
        if (path == null || path.isBlank()) throw new ValidationException("Path cannot be empty");

        String normalizedPath = stripStagePrefix(request, path);

        boolean allowed = allowedPrefixes.stream().anyMatch(normalizedPath::startsWith);
        if (!allowed) throw new ValidationException("Invalid path: " + normalizedPath);

        validateNext(request);
    }

    private String stripStagePrefix(APIGatewayProxyRequestEvent request, String path) {
        if (request.getRequestContext() == null) return path;
        String stage = request.getRequestContext().getStage();
        if (stage == null || stage.isBlank()) return path;

        String stagePrefix = "/" + stage;
        if (path.startsWith(stagePrefix + "/")) return path.substring(stagePrefix.length());
        return path;
    }
}
