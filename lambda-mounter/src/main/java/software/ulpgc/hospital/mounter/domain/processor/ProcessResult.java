package software.ulpgc.hospital.mounter.domain.processor;

public class ProcessResult {
    private final String eventId;
    private final String datamartLocation;
    private final boolean success;

    public ProcessResult(String eventId, String datamartLocation, boolean success) {
        this.eventId = eventId;
        this.datamartLocation = datamartLocation;
        this.success = success;
    }

    public String getEventId() {
        return eventId;
    }

    public String getDatamartLocation() {
        return datamartLocation;
    }

    public boolean isSuccess() {
        return success;
    }
}
