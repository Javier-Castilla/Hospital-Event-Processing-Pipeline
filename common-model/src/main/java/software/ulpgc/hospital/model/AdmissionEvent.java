package software.ulpgc.hospital.model;

import java.sql.Timestamp;
import java.util.UUID;

public record AdmissionEvent(UUID id, Department department, AdmissionType admissionType, String bedNumber, Timestamp timestamp) implements Event {
    @Override
    public UUID getStreamId() {
        return this.id;
    }

    @Override
    public Timestamp getTimestamp() {
        return this.timestamp;
    }

    @Override
    public EventType getEventType() {
        return EventType.ADMISSION;
    }

    public enum AdmissionType {
        EMERGENCY,
        SCHEDULED,
        TRANSFER
    }
}
