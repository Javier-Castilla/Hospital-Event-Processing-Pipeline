package software.ulpgc.hospital.ingest.domain.processor;

public class ProcessResult {
    private final String eventId;
    private final String storageLocation;
    private final boolean success;

    public ProcessResult(String eventId, String storageLocation, boolean success) {
        this.eventId = eventId;
        this.storageLocation = storageLocation;
        this.success = success;
    }

    public String getEventId() {
        return eventId;
    }

    public String getStorageLocation() {
        return storageLocation;
    }

    public boolean isSuccess() {
        return success;
    }
}
