package core.framework.test.assertion;

import core.framework.impl.validate.Validator;
import core.framework.validate.ValidationException;
import org.assertj.core.api.AbstractAssert;

import java.lang.reflect.Field;

/**
 * @author neo
 */
public class ValidatorAssertion extends AbstractAssert<ValidatorAssertion, Object> {
    ValidatorAssertion(Object actual) {
        super(actual, ValidatorAssertion.class);
    }

    public ValidatorAssertion isValid() {
        isNotNull();
        try {
            new Validator(actual.getClass(), Field::getName).validate(actual);
        } catch (ValidationException e) {
            failWithMessage("%nExpecting:%n object %s%nto be valid, but found some violations:%n %s", actual.getClass().getName(), e.errors);
        }
        return this;
    }
}
