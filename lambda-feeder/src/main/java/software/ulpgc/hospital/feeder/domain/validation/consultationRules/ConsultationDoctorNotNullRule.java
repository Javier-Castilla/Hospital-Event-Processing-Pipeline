package software.ulpgc.hospital.feeder.app.validator.rules;

import software.ulpgc.hospital.feeder.app.validator.ValidationRule;
import software.ulpgc.hospital.feeder.domain.validation.ValidationResult;
import software.ulpgc.hospital.model.ConsultationEvent;

public class ConsultationDoctorNotNullRule extends ValidationRule {
    private final ConsultationEvent event;

    private ConsultationDoctorNotNullRule(ConsultationEvent event) {
        this.event = event;
    }

    public static ConsultationDoctorNotNullRule of(ConsultationEvent event) {
        return new ConsultationDoctorNotNullRule(event);
    }

    @Override
    protected ValidationResult check() {
        return event.doctor() == null
            ? ValidationResult.failure("Doctor cannot be null")
            : ValidationResult.success();
    }
}
