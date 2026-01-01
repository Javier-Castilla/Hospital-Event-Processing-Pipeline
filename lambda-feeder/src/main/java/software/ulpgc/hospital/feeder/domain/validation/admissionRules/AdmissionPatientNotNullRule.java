package software.ulpgc.hospital.feeder.app.validator.rules;

import software.ulpgc.hospital.feeder.app.validator.ValidationRule;
import software.ulpgc.hospital.feeder.domain.validation.ValidationResult;
import software.ulpgc.hospital.model.AdmissionEvent;

public class AdmissionPatientNotNullRule extends ValidationRule {
    private final AdmissionEvent event;

    private AdmissionPatientNotNullRule(AdmissionEvent event) {
        this.event = event;
    }

    public static AdmissionPatientNotNullRule of(AdmissionEvent event) {
        return new AdmissionPatientNotNullRule(event);
    }

    @Override
    protected ValidationResult check() {
        return event.patient() == null 
            ? ValidationResult.failure("Patient cannot be null")
            : ValidationResult.success();
    }
}
