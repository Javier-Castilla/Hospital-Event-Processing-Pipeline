package software.ulpgc.hospital.ingest.domain.storage;

public class StorageResult {
    private final String location;
    private final boolean success;

    public StorageResult(String location, boolean success) {
        this.location = location;
        this.success = success;
    }

    public String getLocation() {
        return location;
    }

    public boolean isSuccess() {
        return success;
    }
}
