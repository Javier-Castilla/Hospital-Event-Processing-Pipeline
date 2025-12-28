package software.ulpgc.hospital.model;

import java.sql.Timestamp;
import java.util.UUID;

public record ConsultationEvent(UUID id, Department department, ConsultationType consultationType, Integer durationMinutes, Timestamp timestamp) implements Event {
    @Override
    public UUID getStreamId() {
        return null;
    }

    @Override
    public Timestamp getTimestamp() {
        return null;
    }

    public enum ConsultationType {
        FIRST_VISIT,
        FOLLOW_UP
    }
}
