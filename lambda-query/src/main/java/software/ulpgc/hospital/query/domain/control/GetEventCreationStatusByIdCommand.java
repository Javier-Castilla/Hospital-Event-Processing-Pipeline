package software.ulpgc.hospital.query.domain.control;

import software.ulpgc.hospital.domain.stage.eventCreation.EventCreationStatus;
import software.ulpgc.hospital.domain.stage.repository.EventCreationStatusRepository;

import java.util.UUID;

public class GetEventCreationStatusByIdCommand implements Command {
    private final Input input;
    private final Output output;
    private final EventCreationStatusRepository eventCreationStatusRepository;

    public GetEventCreationStatusByIdCommand(Input input, Output output, EventCreationStatusRepository eventCreationStatusRepository) {
        this.input = input;
        this.output = output;
        this.eventCreationStatusRepository = eventCreationStatusRepository;
    }

    @Override
    public Response execute() {
        return eventCreationStatusRepository.findById(input.id())
                .map(output::result)
                .orElseGet(() -> output.result(null));
    }

    public interface Input {
        UUID id();
    }

    public interface Output {
        Response result(EventCreationStatus eventCreationStatus);
    }
}
