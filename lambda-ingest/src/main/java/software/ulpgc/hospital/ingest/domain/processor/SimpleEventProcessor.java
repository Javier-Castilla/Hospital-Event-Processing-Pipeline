package software.ulpgc.hospital.ingest.domain.processor;

import software.ulpgc.hospital.ingest.domain.storage.EventStorage;
import software.ulpgc.hospital.ingest.domain.storage.StorageResult;
import software.ulpgc.hospital.model.Event;
import software.ulpgc.hospital.model.serialization.EventDeserializer;

public class SimpleEventProcessor implements EventProcessor {
    private final EventDeserializer<Event> deserializer;
    private final EventStorage storage;

    public SimpleEventProcessor(EventDeserializer<Event> deserializer, EventStorage storage) {
        this.deserializer = deserializer;
        this.storage = storage;
    }

    @Override
    public ProcessResult process(String messageBody) throws ProcessException {
        try {
            Event event = deserializer.deserialize(messageBody);

            StorageResult storageResult = storage.store(event);

            return new ProcessResult(
                    event.getStreamId().toString(),
                    storageResult.getLocation(),
                    storageResult.isSuccess()
            );

        } catch (Exception e) {
            throw new ProcessException("Failed to process event: " + e.getMessage(), e);
        }
    }
}
