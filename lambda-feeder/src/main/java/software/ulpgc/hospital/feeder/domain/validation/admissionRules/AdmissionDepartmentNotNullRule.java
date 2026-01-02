package software.ulpgc.hospital.feeder.domain.validation.admissionRules;

import software.ulpgc.hospital.feeder.domain.validation.ValidationRule;
import software.ulpgc.hospital.feeder.domain.validation.ValidationResult;
import software.ulpgc.hospital.domain.model.AdmissionEvent;

public class AdmissionDepartmentNotNullRule extends ValidationRule<AdmissionEvent> {

    private AdmissionDepartmentNotNullRule() {
    }

    public static AdmissionDepartmentNotNullRule create() {
        return new AdmissionDepartmentNotNullRule();
    }

    @Override
    protected ValidationResult check(AdmissionEvent event) {
        return event.admissionDetails().department() == null
            ? ValidationResult.failure("Department cannot be null")
            : ValidationResult.success();
    }
}
