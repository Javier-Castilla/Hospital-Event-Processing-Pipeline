package software.ulpgc.hospital.feeder.domain.validation;

import software.ulpgc.hospital.feeder.app.validator.ValidationRule;
import software.ulpgc.hospital.model.AdmissionEvent;

public class AdmissionEventValidation implements EventValidation<AdmissionEvent> {
    private ValidationRule<AdmissionEvent> first;
    private ValidationRule<AdmissionEvent> last;

    private AdmissionEventValidation() {
    }

    public static AdmissionEventValidation create() {
        return new AdmissionEventValidation();
    }

    @Override
    public void validate(AdmissionEvent event) {
        this.first.validate(event);
    }

    public AdmissionEventValidation next(ValidationRule<AdmissionEvent> rule) {
        if (this.first == null) this.first = rule;
        else this.last.setNext(rule);
        this.last = rule;
        return this;
    }
}
