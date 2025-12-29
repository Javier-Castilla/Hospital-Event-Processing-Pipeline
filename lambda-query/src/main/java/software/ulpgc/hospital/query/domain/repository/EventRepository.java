package software.ulpgc.hospital.query.domain.repository;

import java.util.List;
import java.util.Map;

public interface EventRepository {
    Map<String, Object> findById(String eventId) throws RepositoryException;
    List<Map<String, Object>> findAll() throws RepositoryException;
}
