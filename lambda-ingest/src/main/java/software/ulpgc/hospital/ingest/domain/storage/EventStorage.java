package software.ulpgc.hospital.ingest.domain.storage;

import software.ulpgc.hospital.domain.model.Event;

public interface EventStorage {
    StorageResult store(Event event, String eventCreationId) throws StorageException;
}
