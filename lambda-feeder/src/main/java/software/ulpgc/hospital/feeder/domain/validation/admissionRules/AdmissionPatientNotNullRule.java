package software.ulpgc.hospital.feeder.domain.validation.admissionRules;

import software.ulpgc.hospital.feeder.domain.validation.ValidationRule;
import software.ulpgc.hospital.feeder.domain.validation.ValidationResult;
import software.ulpgc.hospital.model.AdmissionEvent;

public class AdmissionPatientNotNullRule extends ValidationRule<AdmissionEvent> {
    private AdmissionPatientNotNullRule() {
    }

    public static AdmissionPatientNotNullRule create() {
        return new AdmissionPatientNotNullRule();
    }

    @Override
    protected ValidationResult check(AdmissionEvent event) {
        return event.patient() == null 
            ? ValidationResult.failure("Patient cannot be null")
            : ValidationResult.success();
    }
}
