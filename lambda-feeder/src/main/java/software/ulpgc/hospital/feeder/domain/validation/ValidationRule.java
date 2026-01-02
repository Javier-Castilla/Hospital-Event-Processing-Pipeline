package software.ulpgc.hospital.feeder.domain.validation;

public abstract class ValidationRule<T> {

    private ValidationRule<T> next;

    public ValidationRule(ValidationRule<T> next) {
        this.next = next;
    }

    public ValidationRule() {
        this.next = null;
    }

    public ValidationRule<T> setNext(ValidationRule<T> next) {
        this.next = next;
        return next;
    }

    public ValidationResult validate(T event) throws ValidationException {
        ValidationResult result = check(event);
        if (!result.isValid()) return result;
        if (next == null) return ValidationResult.success();
        return next.validate(event);
    }

    protected abstract ValidationResult check(T event);
}
