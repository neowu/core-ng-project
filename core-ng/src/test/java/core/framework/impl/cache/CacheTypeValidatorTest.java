package core.framework.impl.cache;

import core.framework.util.Types;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
class CacheTypeValidatorTest {
    @Test
    void validate() {
        new CacheTypeValidator(CacheItem.class).validate();
    }

    @Test
    void validateListType() {
        new CacheTypeValidator(Types.list(CacheItem.class)).validate();
    }

    @Test
    void validateValueType() {
        new CacheTypeValidator(String.class).validate();
    }

    public static class CacheItem {
        public LocalDateTime dateTimeField;

        public String stringField;

        public List<String> listField;

        public Map<String, String> mapField;

        public Child childField;

        public List<Child> childrenField;
    }

    public static class Child {
        public BigDecimal bigDecimalField = BigDecimal.ZERO;
    }
}
