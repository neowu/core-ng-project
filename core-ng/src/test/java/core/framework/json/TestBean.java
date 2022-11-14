package core.framework.json;

import core.framework.api.json.Property;
import core.framework.api.validate.NotNull;
import core.framework.util.Lists;
import core.framework.util.Maps;

import java.time.Instant;
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
    @NotNull
    @Property(name = "map")
    public Map<String, String> mapField = Maps.newHashMap();

    @NotNull
    @Property(name = "enumMap")
    public Map<TestEnum, String> enumMapField = Maps.newEnumMap(TestEnum.class);

    @NotNull
    @Property(name = "listMap")
    public Map<String, List<String>> mapListField = Maps.newHashMap();

    @NotNull
    @Property(name = "list")
    public List<String> listField = Lists.newArrayList();

    @NotNull
    @Property(name = "children")
    public List<Child> childrenField = Lists.newArrayList();

    @Property(name = "child")
    public Child childField;

    @Property(name = "string")
    public String stringField;

    @Property(name = "date")
    public LocalDate dateField;

    @Property(name = "dateTime")
    public LocalDateTime dateTimeField;

    @Property(name = "zonedDateTime")
    public ZonedDateTime zonedDateTimeField;

    @Property(name = "instant")
    public Instant instantField;

    @Property(name = "time")
    public LocalTime timeField;

    @Property(name = "enum")
    public TestEnum enumField;

    @Property(name = "empty")
    public Empty empty;

    @NotNull
    @Property(name = "defaultValue")
    public String defaultValueField = "defaultValue";

    public enum TestEnum {
        @Property(name = "A1")
        A,
        @Property(name = "B1")
        B,
        @Property(name = "C")
        C
    }

    public static class Child {
        @Property(name = "boolean")
        public Boolean booleanField;

        @Property(name = "long")
        public Long longField;

        @Property(name = "double")
        public Double doubleField;
    }

    public static class Empty {

    }
}
