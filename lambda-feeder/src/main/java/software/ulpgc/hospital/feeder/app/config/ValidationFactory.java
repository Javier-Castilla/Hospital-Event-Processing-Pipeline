package software.ulpgc.hospital.feeder.app.config;

import software.ulpgc.hospital.feeder.domain.validation.EventValidation;
import software.ulpgc.hospital.feeder.domain.validation.ValidationResult;
import software.ulpgc.hospital.domain.model.Event;
import software.ulpgc.hospital.domain.model.EventType;

import java.util.EnumMap;
import java.util.Map;

public class ValidationFactory {
    private final Map<EventType, EventValidation<?>> validations;

    private ValidationFactory(Map<EventType, EventValidation<?>> validations) {
        this.validations = validations;
    }

    public static ValidationFactory create() {
        return new ValidationFactory(new EnumMap<>(EventType.class));
    }

    public ValidationFactory register(EventType eventType, EventValidation<?> validation) {
        this.validations.put(eventType, validation);
        return this;
    }

    public ValidationResult validate(Event event) {
        EventValidation<?> validation = this.validations.get(event.getEventType());
        if (validation == null)
            return ValidationResult.failure("No validation registered for eventType: " + event.getEventType());
        return validation.validateEvent(event);
    }
}
