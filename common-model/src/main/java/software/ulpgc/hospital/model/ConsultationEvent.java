package software.ulpgc.hospital.model;

import java.sql.Timestamp;
import java.util.UUID;

public record ConsultationEvent(
        UUID id,
        PatientSnapshot patient,
        DoctorSnapshot doctor,
        ConsultationDetails consultationDetails,
        Timestamp timestamp
) implements Event {

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

    public record DoctorSnapshot(
            UUID doctorId,
            String name,
            String specialization
    ) {}

    public record ConsultationDetails(
            Department department,
            ConsultationType consultationType,
            String reason,
            String diagnosis,
            String treatment,
            String prescriptions,
            Integer durationMinutes,
            String notes
    ) {}

    public enum ConsultationType {
        FIRST_VISIT,
        FOLLOW_UP,
        URGENT,
        ROUTINE_CHECKUP,
        POST_OPERATIVE
    }
}
