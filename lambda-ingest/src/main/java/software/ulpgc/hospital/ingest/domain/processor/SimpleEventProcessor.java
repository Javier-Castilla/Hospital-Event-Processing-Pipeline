package software.ulpgc.hospital.ingest.domain.processor;

import software.ulpgc.hospital.ingest.domain.storage.EventStorage;
import software.ulpgc.hospital.ingest.domain.storage.StorageResult;
import software.ulpgc.hospital.domain.model.Event;
import software.ulpgc.hospital.domain.model.serialization.EventDeserializer;

import java.util.Map;

public class SimpleEventProcessor implements EventProcessor {
    private final EventDeserializer deserializer;
    private final EventStorage storage;

    public SimpleEventProcessor(EventDeserializer deserializer, EventStorage storage) {
        this.deserializer = deserializer;
        this.storage = storage;
    }

    @Override
    public ProcessResult process(String messageBody, Map<String, String> attributes) throws ProcessException {
        try {
            if (messageBody == null || messageBody.isEmpty()) throw new ProcessException("Message body is null or empty");
            if (attributes == null || !attributes.containsKey("eventCreationId")) throw new ProcessException("Attribute 'eventCreationId' is null or empty");
            Event event = deserializer.deserialize(messageBody);
            StorageResult storageResult = storage.store(event, attributes.get("eventCreationId"));
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
