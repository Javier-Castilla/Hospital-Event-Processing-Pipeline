package software.ulpgc.hospital.app.implementation.serialization;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.ulpgc.hospital.domain.model.*;
import software.ulpgc.hospital.domain.model.serialization.EventDeserializer;
import software.ulpgc.hospital.domain.model.serialization.SerializationException;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

public class JacksonEventDeserializer implements EventDeserializer {

    private final ObjectMapper mapper;

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record PatientSnapshotDTO(
            @JsonProperty("patientId") String patientId,
            @JsonProperty("name") String name,
            @JsonProperty("surname") String surname,
            @JsonProperty("age") Integer age,
            @JsonProperty("gender") String gender,
            @JsonProperty("nationalId") String nationalId
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record AdmissionDetailsDTO(
            @JsonProperty("department") String department,
            @JsonProperty("admissionType") String admissionType,
            @JsonProperty("bedNumber") String bedNumber,
            @JsonProperty("ward") String ward,
            @JsonProperty("attendingPhysician") String attendingPhysician,
            @JsonProperty("initialDiagnosis") String initialDiagnosis
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record AdmissionEventDTO(
            @JsonProperty("id") String id,
            @JsonProperty("patient") PatientSnapshotDTO patient,
            @JsonProperty("admissionDetails") AdmissionDetailsDTO admissionDetails,
            @JsonProperty("timestamp") Object timestamp
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record DoctorSnapshotDTO(
            @JsonProperty("doctorId") String doctorId,
            @JsonProperty("name") String name,
            @JsonProperty("specialization") String specialization
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ConsultationDetailsDTO(
            @JsonProperty("department") String department,
            @JsonProperty("consultationType") String consultationType,
            @JsonProperty("reason") String reason,
            @JsonProperty("diagnosis") String diagnosis,
            @JsonProperty("treatment") String treatment,
            @JsonProperty("prescriptions") String prescriptions,
            @JsonProperty("durationMinutes") Integer durationMinutes,
            @JsonProperty("notes") String notes
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ConsultationEventDTO(
            @JsonProperty("id") String id,
            @JsonProperty("patient") PatientSnapshotDTO patient,
            @JsonProperty("doctor") DoctorSnapshotDTO doctor,
            @JsonProperty("consultationDetails") ConsultationDetailsDTO consultationDetails,
            @JsonProperty("timestamp") Object timestamp
    ) {}

    public JacksonEventDeserializer() {
        this.mapper = new ObjectMapper();
    }

    @Override
    public Event deserialize(String json) throws SerializationException {
        try {
            JsonNode node = mapper.readTree(json);
            String eventType = requireText(node, "eventType");

            return switch (eventType) {
                case "ADMISSION" -> deserializeAdmission(node);
                case "CONSULTATION" -> deserializeConsultation(node);
                default -> throw new SerializationException("Unknown event type: " + eventType, null);
            };
        } catch (SerializationException e) {
            throw e;
        } catch (Exception e) {
            throw new SerializationException("Failed to deserialize event: " + e.getMessage(), e);
        }
    }

    private AdmissionEvent deserializeAdmission(JsonNode node) throws SerializationException {
        AdmissionEventDTO dto = treeTo(node, AdmissionEventDTO.class);

        PatientSnapshotDTO p = require(dto.patient(), "patient");
        AdmissionDetailsDTO a = require(dto.admissionDetails(), "admissionDetails");

        PatientSnapshot patient = new PatientSnapshot(
                parseUuid(p.patientId(), "patient.patientId"),
                requireText(p.name(), "patient.name"),
                requireText(p.surname(), "patient.surname"),
                requireInt(p.age(), "patient.age"),
                requireText(p.gender(), "patient.gender"),
                requireText(p.nationalId(), "patient.nationalId")
        );

        AdmissionEvent.AdmissionDetails admissionDetails = new AdmissionEvent.AdmissionDetails(
                parseEnum(Department.class, a.department(), "admissionDetails.department"),
                parseEnum(AdmissionEvent.AdmissionType.class, a.admissionType(), "admissionDetails.admissionType"),
                requireText(a.bedNumber(), "admissionDetails.bedNumber"),
                requireText(a.ward(), "admissionDetails.ward"),
                requireText(a.attendingPhysician(), "admissionDetails.attendingPhysician"),
                requireText(a.initialDiagnosis(), "admissionDetails.initialDiagnosis")
        );

        return new AdmissionEvent(
                parseUuid(dto.id(), "id"),
                patient,
                admissionDetails,
                parseTimestamp(dto.timestamp(), "timestamp")
        );
    }

    private ConsultationEvent deserializeConsultation(JsonNode node) throws SerializationException {
        ConsultationEventDTO dto = treeTo(node, ConsultationEventDTO.class);

        PatientSnapshotDTO p = require(dto.patient(), "patient");
        DoctorSnapshotDTO d = require(dto.doctor(), "doctor");
        ConsultationDetailsDTO c = require(dto.consultationDetails(), "consultationDetails");

        PatientSnapshot patient = new PatientSnapshot(
                parseUuid(p.patientId(), "patient.patientId"),
                requireText(p.name(), "patient.name"),
                requireText(p.surname(), "patient.surname"),
                requireInt(p.age(), "patient.age"),
                requireText(p.gender(), "patient.gender"),
                requireText(p.nationalId(), "patient.nationalId")
        );

        ConsultationEvent.DoctorSnapshot doctor = new ConsultationEvent.DoctorSnapshot(
                parseUuid(d.doctorId(), "doctor.doctorId"),
                requireText(d.name(), "doctor.name"),
                requireText(d.specialization(), "doctor.specialization")
        );

        ConsultationEvent.ConsultationDetails consultationDetails = new ConsultationEvent.ConsultationDetails(
                parseEnum(Department.class, c.department(), "consultationDetails.department"),
                parseEnum(ConsultationEvent.ConsultationType.class, c.consultationType(), "consultationDetails.consultationType"),
                requireText(c.reason(), "consultationDetails.reason"),
                requireText(c.diagnosis(), "consultationDetails.diagnosis"),
                requireText(c.treatment(), "consultationDetails.treatment"),
                requireText(c.prescriptions(), "consultationDetails.prescriptions"),
                requireInt(c.durationMinutes(), "consultationDetails.durationMinutes"),
                requireText(c.notes(), "consultationDetails.notes")
        );

        return new ConsultationEvent(
                parseUuid(dto.id(), "id"),
                patient,
                doctor,
                consultationDetails,
                parseTimestamp(dto.timestamp(), "timestamp")
        );
    }

    private <T> T treeTo(JsonNode node, Class<T> clazz) throws SerializationException {
        try {
            return mapper.treeToValue(node, clazz);
        } catch (Exception e) {
            throw new SerializationException("Invalid JSON structure for " + clazz.getSimpleName(), e);
        }
    }

    private <T> T require(T value, String fieldPath) throws SerializationException {
        if (value == null) throw new SerializationException("Missing required field: " + fieldPath, null);
        return value;
    }

    private String requireText(JsonNode node, String fieldName) throws SerializationException {
        JsonNode v = node.get(fieldName);
        if (v == null || v.isNull()) throw new SerializationException("Missing required field: " + fieldName, null);
        String s = v.asText();
        if (s == null || s.isBlank()) throw new SerializationException("Missing required field: " + fieldName, null);
        return s;
    }

    private String requireText(String value, String fieldPath) throws SerializationException {
        if (value == null || value.isBlank()) throw new SerializationException("Missing required field: " + fieldPath, null);
        return value;
    }

    private int requireInt(Integer value, String fieldPath) throws SerializationException {
        if (value == null) throw new SerializationException("Missing required field: " + fieldPath, null);
        return value;
    }

    private UUID parseUuid(String raw, String fieldPath) throws SerializationException {
        String v = requireText(raw, fieldPath);
        try {
            return UUID.fromString(v);
        } catch (Exception e) {
            throw new SerializationException("Invalid uuid at " + fieldPath + ": " + v, e);
        }
    }

    private <E extends Enum<E>> E parseEnum(Class<E> enumClass, String raw, String fieldPath) throws SerializationException {
        String v = requireText(raw, fieldPath);
        try {
            return Enum.valueOf(enumClass, v);
        } catch (Exception e) {
            throw new SerializationException("Invalid enum value at " + fieldPath + ": " + v, e);
        }
    }

    private Timestamp parseTimestamp(Object timestamp, String fieldPath) throws SerializationException {
        if (timestamp == null) throw new SerializationException("Missing required field: " + fieldPath, null);

        try {
            if (timestamp instanceof Number n) {
                return new Timestamp(n.longValue());
            }
            if (timestamp instanceof String s) {
                return Timestamp.from(Instant.parse(s));
            }
        } catch (Exception e) {
            throw new SerializationException("Invalid timestamp at " + fieldPath + ": " + timestamp, e);
        }

        throw new SerializationException("Invalid timestamp at " + fieldPath + ": " + timestamp, null);
    }
}
