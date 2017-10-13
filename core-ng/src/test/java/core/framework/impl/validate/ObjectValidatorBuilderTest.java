package core.framework.impl.validate;

import core.framework.util.Types;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertFalse;

/**
 * @author neo
 */
public class ObjectValidatorBuilderTest {
    @Test
    public void withoutValidationAnnotation() {
        assertFalse(new ObjectValidatorBuilder(Bean.class, Field::getName).build().isPresent());
        assertFalse(new ObjectValidatorBuilder(Types.list(Bean.class), Field::getName).build().isPresent());
    }

    public static class Bean {
        public String stringField;
    }
}
