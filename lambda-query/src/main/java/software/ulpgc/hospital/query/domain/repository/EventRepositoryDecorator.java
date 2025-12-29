package software.ulpgc.hospital.query.domain.repository;

import java.util.List;
import java.util.Map;

public abstract class EventRepositoryDecorator implements EventRepository {
    protected final EventRepository wrapped;

    public EventRepositoryDecorator(EventRepository wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public Map<String, Object> findById(String eventId) throws RepositoryException {
        return wrapped.findById(eventId);
    }

    @Override
    public List<Map<String, Object>> findAll() throws RepositoryException {
        return wrapped.findAll();
    }
}
