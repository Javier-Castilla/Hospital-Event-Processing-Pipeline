package software.ulpgc.hospital.feeder.domain.validation.consultationRules;

import software.ulpgc.hospital.feeder.domain.validation.ValidationRule;
import software.ulpgc.hospital.feeder.domain.validation.ValidationResult;
import software.ulpgc.hospital.domain.model.ConsultationEvent;

public class ConsultationDetailsNotNullRule extends ValidationRule<ConsultationEvent> {
    private ConsultationDetailsNotNullRule() {
    }

    public static ConsultationDetailsNotNullRule create() {
        return new ConsultationDetailsNotNullRule();
    }

    @Override
    protected ValidationResult check(ConsultationEvent event) {
        return event.consultationDetails() == null
                ? ValidationResult.failure("Consultation details cannot be null")
                : ValidationResult.success();
    }
}
