package core.framework.impl.validate;

import core.framework.util.Types;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author neo
 */
class ObjectValidatorBuilderTest {
    @Test
    void withoutValidationAnnotation() {
        assertFalse(new ObjectValidatorBuilder(Bean.class, Field::getName).build().isPresent());
        assertFalse(new ObjectValidatorBuilder(Types.list(Bean.class), Field::getName).build().isPresent());
    }

    public static class Bean {
        public String stringField;
    }
}
