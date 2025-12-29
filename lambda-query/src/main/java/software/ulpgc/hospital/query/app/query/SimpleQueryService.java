package software.ulpgc.hospital.query.app.query;

import software.ulpgc.hospital.query.domain.query.QueryException;
import software.ulpgc.hospital.query.domain.query.QueryResult;
import software.ulpgc.hospital.query.domain.query.QueryService;
import software.ulpgc.hospital.query.domain.repository.EventRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SimpleQueryService implements QueryService {
    private final EventRepository repository;

    public SimpleQueryService(EventRepository repository) {
        this.repository = repository;
    }

    @Override
    public QueryResult getEventById(String eventId) throws QueryException {
        try {
            Map<String, Object> event = repository.findById(eventId);
            if (event == null || event.isEmpty()) {
                return new QueryResult(Collections.emptyList(), 0, false);
            }
            return new QueryResult(List.of(event), 1, true);
        } catch (Exception e) {
            throw new QueryException("Failed to query event by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public QueryResult getAllEvents() throws QueryException {
        try {
            List<Map<String, Object>> events = repository.findAll();
            return new QueryResult(events, events.size(), true);
        } catch (Exception e) {
            throw new QueryException("Failed to query all events: " + e.getMessage(), e);
        }
    }
}
