package software.ulpgc.hospital.query.domain.control;

import software.ulpgc.hospital.domain.model.Event;
import software.ulpgc.hospital.query.domain.repository.EventRepository;

import java.util.List;
import java.util.Map;

public class QueryEventsCommand implements Command {
    private final Input input;
    private final Output output;
    private final EventRepository eventRepository;

    public QueryEventsCommand(Input input, Output output, EventRepository eventRepository) {
        this.input = input;
        this.output = output;
        this.eventRepository = eventRepository;
    }

    @Override
    public Response execute() {
        try {
            return this.output.result(this.eventRepository.query(this.input.query()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to query events", e);
        }
    }

    public interface Input {
        Map<String, String> query();
    }

    public interface Output {
        Response result(List<Event> events);
    }
}
