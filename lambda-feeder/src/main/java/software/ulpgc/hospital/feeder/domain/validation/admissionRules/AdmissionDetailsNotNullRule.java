package software.ulpgc.hospital.feeder.domain.validation.admissionRules;

import software.ulpgc.hospital.feeder.domain.validation.ValidationRule;
import software.ulpgc.hospital.feeder.domain.validation.ValidationResult;
import software.ulpgc.hospital.model.AdmissionEvent;

public class AdmissionDetailsNotNullRule extends ValidationRule<AdmissionEvent> {

    private AdmissionDetailsNotNullRule() {
    }

    public static AdmissionDetailsNotNullRule create() {
        return new AdmissionDetailsNotNullRule();
    }

    @Override
    protected ValidationResult check(AdmissionEvent event) {
        return event.admissionDetails() == null
            ? ValidationResult.failure("Admission details cannot be null")
            : ValidationResult.success();
    }
}
