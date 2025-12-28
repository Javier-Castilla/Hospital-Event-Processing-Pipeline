package software.ulpgc.hospital.feeder.app.validator;

import software.ulpgc.hospital.feeder.domain.validator.EventValidator;
import software.ulpgc.hospital.feeder.domain.validator.ValidationResult;
import software.ulpgc.hospital.model.Event;
import software.ulpgc.hospital.model.AdmissionEvent;
import software.ulpgc.hospital.model.ConsultationEvent;

public class JsonEventValidator implements EventValidator {

    @Override
    public ValidationResult validate(Event event) {
        if (event == null) {
            return ValidationResult.failure("Event cannot be null");
        }

        if (event.getStreamId() == null) {
            return ValidationResult.failure("Event ID cannot be null");
        }

        if (event.getTimestamp() == null) {
            return ValidationResult.failure("Timestamp cannot be null");
        }

        return validateSpecificEvent(event);
    }

    private ValidationResult validateSpecificEvent(Event event) {
        return switch (event) {
            case AdmissionEvent admission -> validateAdmission(admission);
            case ConsultationEvent consultation -> validateConsultation(consultation);
            default -> ValidationResult.failure("Unknown event type");
        };
    }

    private ValidationResult validateAdmission(AdmissionEvent event) {
        if (event.department() == null) {
            return ValidationResult.failure("Department cannot be null");
        }
        if (event.admissionType() == null) {
            return ValidationResult.failure("Admission type cannot be null");
        }
        if (event.bedNumber() == null || event.bedNumber().isBlank()) {
            return ValidationResult.failure("Bed number cannot be empty");
        }
        return ValidationResult.success();
    }

    private ValidationResult validateConsultation(ConsultationEvent event) {
        if (event.department() == null) {
            return ValidationResult.failure("Department cannot be null");
        }
        if (event.consultationType() == null) {
            return ValidationResult.failure("Consultation type cannot be null");
        }
        if (event.durationMinutes() == null || event.durationMinutes() <= 0) {
            return ValidationResult.failure("Duration must be positive");
        }
        return ValidationResult.success();
    }

}
