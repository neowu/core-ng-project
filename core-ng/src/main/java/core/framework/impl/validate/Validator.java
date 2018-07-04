package core.framework.impl.validate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.function.Function;

/**
 * @author neo
 */
public final class Validator {
    private final Logger logger = LoggerFactory.getLogger(Validator.class);
    private final BeanValidator validator;

    public Validator(Class<?> beanClass, Function<Field, String> fieldNameProvider) {
        var builder = new BeanValidatorBuilder(beanClass, fieldNameProvider);
        this.validator = builder.build().orElse(null);
    }

    public void validate(Object bean) {
        validate(bean, false);
    }

    private void validate(Object bean, boolean partial) {
        ValidationErrors errors = new ValidationErrors();
        if (bean == null) {
            errors.add("bean", "bean must not be null");
        } else if (validator != null) { // validator can be null if no validation annotation presents
            logger.debug("validate, beanClass={}", bean.getClass().getCanonicalName());
            validator.validate(bean, errors, partial);
        }
        if (errors.hasError())
            throw new ValidationException(errors.errors);
    }

    public void partialValidate(Object bean) {
        validate(bean, true);
    }
}
