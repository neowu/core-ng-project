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
        validate(instance, false);
    }

    private void validate(Object instance, boolean partial) {
        ValidationErrors errors = new ValidationErrors();
        validate(instance, errors, partial);
        if (errors.hasError())
            throw new ValidationException(errors.errors);
    }

    void validate(Object instance, ValidationErrors errors, boolean partial) {
        if (instance == null) {
            errors.add("instance", "instance must not be null");
        } else if (validator != null) { // validator can be null if no validation annotation presents
            validator.validate(instance, errors, partial);
        }
    }

    public void partialValidate(Object instance) {
        validate(instance, true);
    }
}
