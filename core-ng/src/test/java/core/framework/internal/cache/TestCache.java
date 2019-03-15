package core.framework.internal.cache;

import core.framework.api.validate.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public class TestCache {
    public LocalDateTime dateTimeField;

    @NotNull
    public String stringField;

    public List<String> listField;

    public Map<String, String> mapField;

    public Child childField;

    public List<Child> childrenField;

    public Map<TestEnum, String> enumMapField;


    public enum TestEnum {
        V1,
        V2
    }

    public static class Child {
        @NotNull
        public BigDecimal bigDecimalField = BigDecimal.ZERO;
    }
}
