package software.ulpgc.hospital.query.app.validation;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import java.util.Map;
import java.util.Set;

public class QueryValidator extends RequestValidator {

    private static final Set<String> VALID_EVENT_FILTERS = Set.of(
            "eventType",
            "date",
            "department",
            "admissionType",
            "bedNumber"
    );

    private static final Set<String> VALID_STATS_FILTERS = Set.of(
            "department",
            "date",
            "minAdmissions"
    );

    @Override
    public void validate(APIGatewayProxyRequestEvent request) throws ValidationException {
        String path = request.getPath();
        Map<String, String> filters = request.getQueryStringParameters();

        if (filters != null && !filters.isEmpty()) {
            if (path.equals("/events")) {
                validateEventFilters(filters);
            } else if (path.equals("/stats")) {
                validateStatsFilters(filters);
            }
        }

        validateNext(request);
    }

    private void validateEventFilters(Map<String, String> filters) throws ValidationException {
        for (String key : filters.keySet()) {
            if (!VALID_EVENT_FILTERS.contains(key)) {
                throw new ValidationException(
                        "Invalid filter for /events: '" + key + "'. Allowed: " + VALID_EVENT_FILTERS
                );
            }
        }
    }

    private void validateStatsFilters(Map<String, String> filters) throws ValidationException {
        for (String key : filters.keySet()) {
            if (!VALID_STATS_FILTERS.contains(key)) {
                throw new ValidationException(
                        "Invalid filter for /stats: '" + key + "'. Allowed: " + VALID_STATS_FILTERS
                );
            }
        }

        if (filters.containsKey("minAdmissions")) {
            try {
                Integer.parseInt(filters.get("minAdmissions"));
            } catch (NumberFormatException e) {
                throw new ValidationException("'minAdmissions' must be a valid number");
            }
        }
    }
}
