package software.ulpgc.hospital.query.domain.repository;

import software.ulpgc.hospital.domain.model.Event;
import java.util.List;
import java.util.Map;

public interface EventRepository {
    List<Event> query(Map<String, String> filters) throws RepositoryException;
}
