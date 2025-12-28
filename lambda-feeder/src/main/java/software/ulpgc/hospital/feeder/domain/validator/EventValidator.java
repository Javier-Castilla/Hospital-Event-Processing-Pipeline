package software.ulpgc.hospital.feeder.domain.validator;

import software.ulpgc.hospital.model.Event;

public interface EventValidator {
    ValidationResult validate(Event event);
}
