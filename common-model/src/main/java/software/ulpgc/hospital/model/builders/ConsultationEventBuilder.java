package software.ulpgc.hospital.model.builders;

import software.ulpgc.hospital.model.ConsultationEvent;
import software.ulpgc.hospital.model.ConsultationEvent.ConsultationType;
import software.ulpgc.hospital.model.Department;

import java.sql.Timestamp;
import java.util.UUID;

public class ConsultationEventBuilder {
    private UUID id;
    private Department department;
    private ConsultationType consultationType;
    private Integer durationMinutes;
    private Timestamp timestamp;

    public ConsultationEventBuilder() {
        this.id = UUID.randomUUID();
        this.timestamp = new Timestamp(System.currentTimeMillis());
    }

    public ConsultationEventBuilder withId(UUID id) {
        this.id = id;
        return this;
    }

    public ConsultationEventBuilder withDepartment(Department department) {
        this.department = department;
        return this;
    }

    public ConsultationEventBuilder withConsultationType(ConsultationType consultationType) {
        this.consultationType = consultationType;
        return this;
    }

    public ConsultationEventBuilder withDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
        return this;
    }

    public ConsultationEventBuilder withTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public ConsultationEvent build() {
        return new ConsultationEvent(id, department, consultationType, durationMinutes, timestamp);
    }
}
