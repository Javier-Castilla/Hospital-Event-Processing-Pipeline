package software.ulpgc.hospital.feeder.domain.validation.consultationRules;

import software.ulpgc.hospital.feeder.domain.validation.ValidationRule;
import software.ulpgc.hospital.feeder.domain.validation.ValidationResult;
import software.ulpgc.hospital.domain.model.ConsultationEvent;

public class ConsultationDurationPositiveRule extends ValidationRule<ConsultationEvent> {
    private ConsultationDurationPositiveRule() {
    }

    public static ConsultationDurationPositiveRule create() {
        return new ConsultationDurationPositiveRule();
    }

    @Override
    protected ValidationResult check(ConsultationEvent event) {
        return event.consultationDetails().durationMinutes() == null || event.consultationDetails().durationMinutes() <= 0
                ? ValidationResult.failure("Duration must be positive")
                : ValidationResult.success();
    }
}
