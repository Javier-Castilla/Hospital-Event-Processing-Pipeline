package software.ulpgc.hospital.model;

import java.sql.Timestamp;
import java.util.UUID;

public record ConsultationEvent(UUID id, Department department, ConsultationType consultationType, Integer durationMinutes, Timestamp timestamp) implements Event {

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
        return EventType.CONSULTATION;
    }

    public enum ConsultationType {
        FIRST_VISIT,
        FOLLOW_UP
    }
}
