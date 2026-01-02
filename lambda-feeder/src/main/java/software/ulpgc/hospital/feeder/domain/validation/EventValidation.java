package software.ulpgc.hospital.feeder.domain.validation;

import software.ulpgc.hospital.domain.model.Event;

public class EventValidation<T extends Event> {
    private final Class<T> type;
    private ValidationRule<T> first;
    private ValidationRule<T> last;

    private EventValidation(Class<T> type) {
        this.type = type;
    }

    public static <T extends Event> EventValidation<T> of(Class<T> type) {
        return new EventValidation<>(type);
    }

    public EventValidation<T> next(ValidationRule<T> rule) {
        if (this.first == null) this.first = rule;
        else this.last.setNext(rule);
        this.last = rule;
        return this;
    }

    public ValidationResult validate(T event) {
        if (this.first == null) return ValidationResult.success();
        return this.first.validate(event);
    }

    public ValidationResult validateEvent(Event event) {
        if (!type.isInstance(event)) {
            return ValidationResult.failure("Invalid event type: " + event.getClass().getSimpleName());
        }
        return validate(type.cast(event));
    }
}
