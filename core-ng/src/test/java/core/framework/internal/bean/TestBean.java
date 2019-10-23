package core.framework.internal.bean;

import core.framework.api.json.Property;
import core.framework.api.validate.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public class TestBean {
    @Property(name = "zoned_date_time_field")
    public ZonedDateTime zonedDateTimeField;

    @Property(name = "date_time_field")
    public LocalDateTime dateTimeField;

    @Property(name = "date_field")
    public LocalDate dateField;

    @Property(name = "time_field")
    public LocalTime timeField;

    @Property(name = "string_field")
    public String stringField;

    @NotNull
    @Property(name = "int_field")
    public Integer intField;

    @Property(name = "big_decimal_field")
    public BigDecimal bigDecimalField;

    @Property(name = "list_field")
    public List<String> listField;

    @Property(name = "map_field")
    public Map<String, String> mapField;

    @Property(name = "enum_map_field")
    public Map<TestEnum, String> enumMapField;

    @Property(name = "child_field")
    public Child childField;

    @Property(name = "children_field")
    public List<Child> childrenField;

    @Property(name = "enum_field")
    public TestEnum enumField;

    public enum TestEnum {
        @Property(name = "V1")
        VALUE1,
        @Property(name = "V2")
        VALUE2
    }

    public static class Child {
        @Property(name = "boolean_field")
        public Boolean booleanField;
    }
}
