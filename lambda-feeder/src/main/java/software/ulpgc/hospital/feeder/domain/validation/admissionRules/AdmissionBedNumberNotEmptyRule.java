package software.ulpgc.hospital.feeder.domain.validation.admissionRules;

import software.ulpgc.hospital.feeder.domain.validation.ValidationRule;
import software.ulpgc.hospital.feeder.domain.validation.ValidationResult;
import software.ulpgc.hospital.model.AdmissionEvent;

public class AdmissionBedNumberNotEmptyRule extends ValidationRule<AdmissionEvent> {
    private AdmissionBedNumberNotEmptyRule() {
    }

    public static AdmissionBedNumberNotEmptyRule create() {
        return new AdmissionBedNumberNotEmptyRule();
    }

    @Override
    protected ValidationResult check(AdmissionEvent event) {
        return event.admissionDetails().bedNumber() == null || event.admissionDetails().bedNumber().isBlank()
            ? ValidationResult.failure("Bed number cannot be empty")
            : ValidationResult.success();
    }
}
