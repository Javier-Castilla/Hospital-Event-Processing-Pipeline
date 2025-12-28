package software.ulpgc.hospital.ingest.domain.storage;

import software.ulpgc.hospital.model.Event;

public interface EventStorage {
    StorageResult store(Event event) throws StorageException;
}
