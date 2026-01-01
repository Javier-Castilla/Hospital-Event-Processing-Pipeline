package software.ulpgc.hospital.feeder.app.validator.rules;

import software.ulpgc.hospital.feeder.app.validator.ValidationRule;
import software.ulpgc.hospital.feeder.domain.validation.ValidationResult;
import software.ulpgc.hospital.model.AdmissionEvent;

public class AdmissionBedNumberNotEmptyRule extends ValidationRule<AdmissionEvent> {
    private AdmissionBedNumberNotEmptyRule() {
    }

    public static AdmissionBedNumberNotEmptyRule create() {
        return new AdmissionBedNumberNotEmptyRule();
    }

    @Override
    protected ValidationResult check(T event) {
        return event.admission().bedNumber() == null || event.admission().bedNumber().isBlank()
            ? ValidationResult.failure("Bed number cannot be empty")
            : ValidationResult.success();
    }
}
