package software.ulpgc.hospital.model.builders;

import software.ulpgc.hospital.model.AdmissionEvent;
import software.ulpgc.hospital.model.AdmissionEvent.AdmissionType;
import software.ulpgc.hospital.model.Department;

import java.sql.Timestamp;
import java.util.UUID;

public class AdmissionEventBuilder {
    private UUID id;
    private Department department;
    private AdmissionType admissionType;
    private String bedNumber;
    private Timestamp timestamp;

    public AdmissionEventBuilder() {
        this.id = UUID.randomUUID();
        this.timestamp = new Timestamp(System.currentTimeMillis());
    }

    public AdmissionEventBuilder withId(UUID id) {
        this.id = id;
        return this;
    }

    public AdmissionEventBuilder withDepartment(Department department) {
        this.department = department;
        return this;
    }

    public AdmissionEventBuilder withAdmissionType(AdmissionType admissionType) {
        this.admissionType = admissionType;
        return this;
    }

    public AdmissionEventBuilder withBedNumber(String bedNumber) {
        this.bedNumber = bedNumber;
        return this;
    }

    public AdmissionEventBuilder withTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public AdmissionEvent build() {
        return new AdmissionEvent(id, department, admissionType, bedNumber, timestamp);
    }
}
