package software.ulpgc.hospital.query.app.repository;

import software.ulpgc.hospital.query.domain.repository.EventRepository;
import software.ulpgc.hospital.query.domain.repository.RepositoryException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockEventRepository implements EventRepository {
    private final Map<String, Map<String, Object>> storage = new HashMap<>();

    @Override
    public Map<String, Object> findById(String eventId) throws RepositoryException {
        return storage.get(eventId);
    }

    @Override
    public List<Map<String, Object>> findAll() throws RepositoryException {
        return new ArrayList<>(storage.values());
    }

    public void addEvent(String id, Map<String, Object> event) {
        storage.put(id, event);
    }
}
