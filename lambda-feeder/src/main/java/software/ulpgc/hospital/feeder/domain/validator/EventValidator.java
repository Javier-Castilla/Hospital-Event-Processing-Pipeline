package software.ulpgc.hospital.feeder.validator;

import software.ulpgc.hospital.model.Event;

public interface EventValidator {
    ValidationResult validate(Event event);
}
