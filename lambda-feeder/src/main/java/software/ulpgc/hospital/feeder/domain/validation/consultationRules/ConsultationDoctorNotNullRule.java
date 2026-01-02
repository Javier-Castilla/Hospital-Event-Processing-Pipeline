package software.ulpgc.hospital.feeder.domain.validation.consultationRules;

import software.ulpgc.hospital.feeder.domain.validation.ValidationRule;
import software.ulpgc.hospital.feeder.domain.validation.ValidationResult;
import software.ulpgc.hospital.model.ConsultationEvent;

public class ConsultationDoctorNotNullRule extends ValidationRule<ConsultationEvent> {
    private ConsultationDoctorNotNullRule() {
    }

    public static ConsultationDoctorNotNullRule create() {
        return new ConsultationDoctorNotNullRule();
    }

    @Override
    protected ValidationResult check(ConsultationEvent event) {
        return event.doctor() == null
                ? ValidationResult.failure("Doctor cannot be null")
                : ValidationResult.success();
    }
}
