package software.ulpgc.hospital.feeder.app.validator.rules;

import software.ulpgc.hospital.feeder.app.validator.ValidationRule;
import software.ulpgc.hospital.feeder.domain.validation.ValidationResult;
import software.ulpgc.hospital.model.ConsultationEvent;

public class ConsultationDurationPositiveRule extends ValidationRule {
    private final ConsultationEvent event;

    private ConsultationDurationPositiveRule(ConsultationEvent event) {
        this.event = event;
    }

    public static ConsultationDurationPositiveRule of(ConsultationEvent event) {
        return new ConsultationDurationPositiveRule(event);
    }

    @Override
    protected ValidationResult check() {
        return event.consultation().durationMinutes() == null || event.consultation().durationMinutes() <= 0
            ? ValidationResult.failure("Duration must be positive")
            : ValidationResult.success();
    }
}
