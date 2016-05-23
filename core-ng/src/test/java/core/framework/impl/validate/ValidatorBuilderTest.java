package core.framework.impl.validate;

import core.framework.api.util.Types;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;

/**
 * @author neo
 */
public class ValidatorBuilderTest {
    @Test
    public void withoutValidationAnnotation() {
        Validator validator = new ValidatorBuilder(Bean.class, Field::getName).build();
        Assert.assertNull(validator.validator);

        validator = new ValidatorBuilder(Types.list(Bean.class), Field::getName).build();
        Assert.assertNull(validator.validator);

        validator = new ValidatorBuilder(Types.list(String.class), Field::getName).build();
        Assert.assertNull(validator.validator);

        validator = new ValidatorBuilder(Types.optional(Bean.class), Field::getName).build();
        Assert.assertNull(validator.validator);
    }

    public static class Bean {
        public String stringField;
    }
}