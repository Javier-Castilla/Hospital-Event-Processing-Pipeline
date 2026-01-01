package software.ulpgc.hospital.feeder.app.validator.rules;

import software.ulpgc.hospital.feeder.app.validator.ValidationRule;
import software.ulpgc.hospital.feeder.domain.validation.ValidationResult;
import software.ulpgc.hospital.model.AdmissionEvent;

public class AdmissionDepartmentNotNullRule extends ValidationRule {
    private final AdmissionEvent event;

    private AdmissionDepartmentNotNullRule(AdmissionEvent event) {
        this.event = event;
    }

    public static AdmissionDepartmentNotNullRule of(AdmissionEvent event) {
        return new AdmissionDepartmentNotNullRule(event);
    }

    @Override
    protected ValidationResult check() {
        return event.admission().department() == null
            ? ValidationResult.failure("Department cannot be null")
            : ValidationResult.success();
    }
}
