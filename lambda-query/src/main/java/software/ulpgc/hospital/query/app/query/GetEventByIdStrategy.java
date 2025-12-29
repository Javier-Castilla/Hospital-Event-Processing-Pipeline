package software.ulpgc.hospital.query.app.query;

import software.ulpgc.hospital.query.domain.query.QueryException;
import software.ulpgc.hospital.query.domain.query.QueryResult;
import software.ulpgc.hospital.query.domain.query.QueryStrategy;
import software.ulpgc.hospital.query.domain.repository.EventRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GetEventByIdStrategy implements QueryStrategy {
    private final EventRepository repository;

    public GetEventByIdStrategy(EventRepository repository) {
        this.repository = repository;
    }

    @Override
    public QueryResult execute(Map<String, String> parameters) throws QueryException {
        try {
            String eventId = parameters.get("eventId");
            if (eventId == null || eventId.isEmpty()) {
                return new QueryResult.Builder()
                    .success(false)
                    .message("Event ID is required")
                    .build();
            }

            Map<String, Object> event = repository.findById(eventId);
            if (event == null || event.isEmpty()) {
                return new QueryResult.Builder()
                    .success(false)
                    .message("Event not found")
                    .build();
            }

            return new QueryResult.Builder()
                .data(List.of(event))
                .message("Event retrieved successfully")
                .build();
        } catch (Exception e) {
            throw new QueryException("Failed to retrieve event by ID: " + e.getMessage(), e);
        }
    }
}
