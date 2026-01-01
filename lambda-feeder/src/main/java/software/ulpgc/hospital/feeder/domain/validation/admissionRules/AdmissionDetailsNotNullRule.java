package software.ulpgc.hospital.feeder.app.validator.rules;

import software.ulpgc.hospital.feeder.app.validator.ValidationRule;
import software.ulpgc.hospital.feeder.domain.validation.ValidationResult;
import software.ulpgc.hospital.model.AdmissionEvent;

public class AdmissionDetailsNotNullRule extends ValidationRule {
    private final AdmissionEvent event;

    private AdmissionDetailsNotNullRule(AdmissionEvent event) {
        this.event = event;
    }

    public static AdmissionDetailsNotNullRule of(AdmissionEvent event) {
        return new AdmissionDetailsNotNullRule(event);
    }

    @Override
    protected ValidationResult check() {
        return event.admission() == null
            ? ValidationResult.failure("Admission details cannot be null")
            : ValidationResult.success();
    }
}
