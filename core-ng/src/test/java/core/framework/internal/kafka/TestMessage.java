package core.framework.internal.kafka;

import core.framework.api.json.Property;
import core.framework.api.validate.NotNull;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public class TestMessage {
    @Property(name = "zoned_date_time_field")
    public ZonedDateTime zonedDateTimeField;

    @Property(name = "date_time_field")
    public LocalDateTime dateTimeField;

    @NotNull
    @Property(name = "string_field")
    public String stringField;

    @Property(name = "list_field")
    public List<String> listField;

    @Property(name = "map_field")
    public Map<String, String> mapField;

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
