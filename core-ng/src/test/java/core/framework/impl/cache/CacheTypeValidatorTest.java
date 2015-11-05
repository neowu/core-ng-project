package core.framework.impl.cache;

import core.framework.api.util.Types;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public class CacheTypeValidatorTest {
    @Test
    public void validate() {
        new CacheTypeValidator(CacheItem.class).validate();
    }

    @Test
    public void validateListType() {
        new CacheTypeValidator(Types.list(CacheItem.class)).validate();
    }

    @Test
    public void validateValueType() {
        new CacheTypeValidator(String.class).validate();
    }

    public static class CacheItem {
        public LocalDateTime dateTimeField;

        public String stringField;

        public List<String> listField;

        public Map<String, String> mapField;

        public CacheChildItem childField;

        public List<CacheChildItem> childrenField;
    }

    public static class CacheChildItem {
        public BigDecimal bigDecimalField = BigDecimal.ZERO;
    }
}