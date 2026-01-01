package software.ulpgc.hospital.feeder.app.validator.rules;

import software.ulpgc.hospital.feeder.app.validator.ValidationRule;
import software.ulpgc.hospital.feeder.domain.validation.ValidationResult;
import software.ulpgc.hospital.model.ConsultationEvent;

public class ConsultationDetailsNotNullRule extends ValidationRule {
    private final ConsultationEvent event;

    private ConsultationDetailsNotNullRule(ConsultationEvent event) {
        this.event = event;
    }

    public static ConsultationDetailsNotNullRule of(ConsultationEvent event) {
        return new ConsultationDetailsNotNullRule(event);
    }

    @Override
    protected ValidationResult check() {
        return event.consultation() == null
            ? ValidationResult.failure("Consultation details cannot be null")
            : ValidationResult.success();
    }
}
