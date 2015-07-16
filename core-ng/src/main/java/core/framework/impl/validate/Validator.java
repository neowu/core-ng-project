package core.framework.impl.validate;

import core.framework.api.validate.ValidationException;

/**
 * @author neo
 */
public class Validator {
    final FieldValidator validator;

    Validator(FieldValidator validator) {
        this.validator = validator;
    }

    public void validate(Object instance) {
        ValidationErrors errors = new ValidationErrors();
        validate(instance, errors);
        if (errors.hasError())
            throw new ValidationException(errors.errors);
    }

    void validate(Object instance, ValidationErrors errors) {
        if (instance == null) {
            errors.add("instance", "instance must not be null");
        } else if (validator != null) { // validator can be null if no validation annotation presents
            validator.validate(instance, errors);
        }
    }
}
