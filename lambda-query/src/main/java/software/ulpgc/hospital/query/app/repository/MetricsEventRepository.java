package software.ulpgc.hospital.query.app.repository;

import software.ulpgc.hospital.query.domain.repository.EventRepository;
import software.ulpgc.hospital.query.domain.repository.EventRepositoryDecorator;
import software.ulpgc.hospital.query.domain.repository.RepositoryException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MetricsEventRepository extends EventRepositoryDecorator {
    private final AtomicInteger findByIdCount = new AtomicInteger(0);
    private final AtomicInteger findAllCount = new AtomicInteger(0);

    public MetricsEventRepository(EventRepository wrapped) {
        super(wrapped);
    }

    @Override
    public Map<String, Object> findById(String eventId) throws RepositoryException {
        findByIdCount.incrementAndGet();
        System.out.println("[METRICS] findById called " + findByIdCount.get() + " times");
        return wrapped.findById(eventId);
    }

    @Override
    public List<Map<String, Object>> findAll() throws RepositoryException {
        findAllCount.incrementAndGet();
        System.out.println("[METRICS] findAll called " + findAllCount.get() + " times");
        return wrapped.findAll();
    }

    public int getFindByIdCount() {
        return findByIdCount.get();
    }

    public int getFindAllCount() {
        return findAllCount.get();
    }
}
