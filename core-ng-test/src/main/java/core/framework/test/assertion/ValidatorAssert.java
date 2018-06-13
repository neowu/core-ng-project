package core.framework.test.assertion;

import core.framework.impl.validate.Validator;
import core.framework.validate.ValidationException;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.MapAssert;

import java.lang.reflect.Field;

/**
 * @author neo
 */
public class ValidatorAssert extends AbstractAssert<ValidatorAssert, Object> {
    public ValidatorAssert(Object actual) {
        super(actual, ValidatorAssert.class);
    }

    public void isValid() {
        isNotNull();
        try {
            validator().validate(actual);
        } catch (ValidationException e) {
            failWithMessage("%nExpecting:%n object %s%nto be valid, but found some violations:%n %s", actual.getClass().getName(), e.errors);
        }
    }

    public MapAssert<String, String> hasError() {
        try {
            validator().validate(actual);
            failWithMessage("%nExpecting:%n object %s%nto be invalid, but found no violation", actual.getClass().getName());
            return null;
        } catch (ValidationException e) {
            return new MapAssert<>(e.errors);
        }
    }

    private Validator validator() {
        return new Validator(actual.getClass(), Field::getName);
    }
}
