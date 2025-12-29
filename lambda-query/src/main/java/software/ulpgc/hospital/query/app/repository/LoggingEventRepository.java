package software.ulpgc.hospital.query.app.repository;

import software.ulpgc.hospital.query.domain.repository.EventRepository;
import software.ulpgc.hospital.query.domain.repository.EventRepositoryDecorator;
import software.ulpgc.hospital.query.domain.repository.RepositoryException;

import java.util.List;
import java.util.Map;

public class LoggingEventRepository extends EventRepositoryDecorator {
    
    public LoggingEventRepository(EventRepository wrapped) {
        super(wrapped);
    }

    @Override
    public Map<String, Object> findById(String eventId) throws RepositoryException {
        System.out.println("[LOG] Finding event by ID: " + eventId);
        long start = System.currentTimeMillis();
        try {
            Map<String, Object> result = wrapped.findById(eventId);
            long duration = System.currentTimeMillis() - start;
            System.out.println("[LOG] Found event in " + duration + "ms");
            return result;
        } catch (RepositoryException e) {
            System.out.println("[LOG] Error finding event: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public List<Map<String, Object>> findAll() throws RepositoryException {
        System.out.println("[LOG] Finding all events");
        long start = System.currentTimeMillis();
        try {
            List<Map<String, Object>> result = wrapped.findAll();
            long duration = System.currentTimeMillis() - start;
            System.out.println("[LOG] Found " + result.size() + " events in " + duration + "ms");
            return result;
        } catch (RepositoryException e) {
            System.out.println("[LOG] Error finding all events: " + e.getMessage());
            throw e;
        }
    }
}
