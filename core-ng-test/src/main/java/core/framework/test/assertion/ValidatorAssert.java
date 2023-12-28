package core.framework.test.assertion;

import core.framework.internal.validate.BeanValidator;
import core.framework.internal.validate.BeanValidatorBuilder;
import core.framework.internal.validate.ValidationErrors;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.MapAssert;

/**
 * @author neo
 */
public final class ValidatorAssert extends AbstractAssert<ValidatorAssert, Object> {
    private final BeanValidator validator;

    public ValidatorAssert(Object actual) {
        super(actual, ValidatorAssert.class);
        isNotNull();
        this.validator = validator(actual);
    }

    public void isValid() {
        ValidationErrors errors = validate();
        if (errors.hasError())
            failWithMessage("%nExpecting:%n object %s%nto be valid, but found some violations:%n %s", actual.getClass().getName(), errors.errors);
    }

    public MapAssert<String, String> errors() {
        ValidationErrors errors = validate();
        if (!errors.hasError())
            failWithMessage("%nExpecting:%n object %s%nto be invalid, but found no violation", actual.getClass().getName());
        return new MapAssert<>(errors.errors);
    }

    private ValidationErrors validate() {
        ValidationErrors errors = new ValidationErrors();
        validator.validate(actual, errors, false);
        return errors;
    }

    private BeanValidator validator(Object bean) {
        Class<?> beanClass = bean.getClass();
        var builder = new BeanValidatorBuilder(beanClass);
        BeanValidator validator = builder.build();
        if (validator == null) {
            failWithMessage("%nExpecting:%n  %s%nhas validation annotations, but was not found", beanClass.getName());
        }
        return validator;
    }
}
