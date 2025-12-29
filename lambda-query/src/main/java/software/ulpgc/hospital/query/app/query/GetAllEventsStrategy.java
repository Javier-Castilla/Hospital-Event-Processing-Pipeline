package software.ulpgc.hospital.query.app.query;

import software.ulpgc.hospital.query.domain.query.QueryException;
import software.ulpgc.hospital.query.domain.query.QueryResult;
import software.ulpgc.hospital.query.domain.query.QueryStrategy;
import software.ulpgc.hospital.query.domain.repository.EventRepository;

import java.util.List;
import java.util.Map;

public class GetAllEventsStrategy implements QueryStrategy {
    private final EventRepository repository;

    public GetAllEventsStrategy(EventRepository repository) {
        this.repository = repository;
    }

    @Override
    public QueryResult execute(Map<String, String> parameters) throws QueryException {
        try {
            List<Map<String, Object>> events = repository.findAll();
            return new QueryResult.Builder()
                .data(events)
                .message("Successfully retrieved all events")
                .build();
        } catch (Exception e) {
            throw new QueryException("Failed to retrieve all events: " + e.getMessage(), e);
        }
    }
}
