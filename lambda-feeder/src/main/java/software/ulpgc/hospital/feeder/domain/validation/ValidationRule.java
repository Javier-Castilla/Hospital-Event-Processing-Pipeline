package software.ulpgc.hospital.feeder.app.validator;

import software.ulpgc.hospital.feeder.domain.validation.ValidationException;
import software.ulpgc.hospital.feeder.domain.validation.ValidationResult;
import software.ulpgc.hospital.model.Event;

public abstract class ValidationRule<T extends Event> {
    private ValidationRule<T> next;

    public ValidationRule(ValidationRule<T> next) {
        this.next = next;
    };

    public ValidationRule() {
        this.next = null;
    };

    public ValidationRule<T> setNext(ValidationRule<T> next) {
        this.next = next;
        return next;
    }

    public ValidationResult validate(T event) throws ValidationException {
        ValidationResult result = check(event);
        if (!result.isValid()) return ValidationResult.failure(result.toString());
        return next.validate(event);
    }

    protected abstract ValidationResult check(T event);
}
