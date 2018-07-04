package core.framework.impl.validate;

import core.framework.validate.ValidationException;

import java.lang.reflect.Field;
import java.util.function.Function;

/**
 * @author neo
 */
public final class Validator {
    // TODO: validate response, make validator return errors, to throw warn/error exceptions
    private final BeanValidator validator;

    public Validator(Class<?> beanClass, Function<Field, String> fieldNameProvider) {
        var builder = new BeanValidatorBuilder(beanClass, fieldNameProvider);
        this.validator = builder.build().orElse(null);
    }

    public void validate(Object instance) {
        validate(instance, false);
    }

    private void validate(Object instance, boolean partial) {
        ValidationErrors errors = new ValidationErrors();
        if (instance == null) {
            errors.add("instance", "instance must not be null");
        } else if (validator != null) { // validator can be null if no validation annotation presents
            validator.validate(instance, errors, partial);
        }
        if (errors.hasError())
            throw new ValidationException(errors.errors);
    }

    public void partialValidate(Object instance) {
        validate(instance, true);
    }
}
