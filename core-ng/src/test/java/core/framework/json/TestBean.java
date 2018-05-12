package core.framework.json;

import core.framework.api.json.Property;
import core.framework.util.Lists;
import core.framework.util.Maps;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
class TestBean {
    @Property(name = "map")
    public final Map<String, String> mapField = Maps.newHashMap();

    @Property(name = "list")
    public final List<String> listField = Lists.newArrayList();

    @Property(name = "children")
    public final List<Child> childrenField = Lists.newArrayList();

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

    public Integer notAnnotatedField;

    @Property(name = "empty")
    public Empty empty;

    enum TestEnum {
        @Property(name = "A1")
        A,
        @Property(name = "B1")
        B,
        C
    }

    static class Child {
        @Property(name = "boolean")
        public Boolean booleanField;

        @Property(name = "long")
        public Long longField;
    }

    static class Empty {

    }
}
