package core.framework.impl.validate;

/**
 * @author neo
 */
public class Validator {
    final FieldValidator validator;

    Validator(FieldValidator validator) {
        this.validator = validator;
    }

    public ValidationResult validate(Object instance) {
        ValidationResult result = new ValidationResult();
        if (instance == null) throw new Error("instance must not be null");
        if (validator != null) validator.validate(instance, result);
        return result;
    }
}
