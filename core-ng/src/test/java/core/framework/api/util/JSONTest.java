package core.framework.api.util;

import core.framework.api.json.Property;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author neo
 */
public class JSONTest {
    @Test
    public void mapField() {
        Bean bean = new Bean();
        bean.mapField.put("key1", "value1");
        bean.mapField.put("key2", "value2");

        String json = JSON.toJSON(bean);
        assertThat(json, containsString("\"map\":{\"key1\":\"value1\",\"key2\":\"value2\"}"));

        Bean parsedBean = JSON.fromJSON(Bean.class, json);
        assertEquals("value1", parsedBean.mapField.get("key1"));
        assertEquals("value2", parsedBean.mapField.get("key2"));
    }

    @Test
    public void listField() {
        Bean bean = new Bean();
        bean.listField.add("value1");
        bean.listField.add("value2");

        Child child1 = new Child();
        child1.booleanField = true;
        bean.childrenField.add(child1);
        Child child2 = new Child();
        child2.booleanField = false;
        bean.childrenField.add(child2);

        String json = JSON.toJSON(bean);
        assertThat(json, containsString("\"list\":[\"value1\",\"value2\"],\"children\":[{\"boolean\":true},{\"boolean\":false}]"));

        Bean parsedBean = JSON.fromJSON(Bean.class, json);
        assertEquals(bean.listField, parsedBean.listField);
        assertEquals(2, parsedBean.childrenField.size());
        assertEquals(true, parsedBean.childrenField.get(0).booleanField);
        assertEquals(false, parsedBean.childrenField.get(1).booleanField);
    }

    @Test
    public void dateField() {
        Bean bean = new Bean();
        bean.instantField = Instant.now();
        bean.dateTimeField = LocalDateTime.ofInstant(bean.instantField, ZoneId.systemDefault());
        bean.dateField = bean.dateTimeField.toLocalDate();
        bean.zonedDateTimeField = ZonedDateTime.ofInstant(bean.instantField, ZoneId.systemDefault());
        String json = JSON.toJSON(bean);

        Bean parsedBean = JSON.fromJSON(Bean.class, json);
        assertEquals(bean.instantField, parsedBean.instantField);
        assertEquals(bean.dateField, parsedBean.dateField);
        assertEquals(bean.dateTimeField, parsedBean.dateTimeField);
        assertEquals(bean.zonedDateTimeField.toInstant(), parsedBean.zonedDateTimeField.toInstant());
    }

    @Test
    public void listObject() {
        List<Bean> beans = JSON.fromJSON(Types.list(Bean.class), "[{\"string\":\"n1\"},{\"string\":\"n2\"}]");

        assertEquals(2, beans.size());
        assertEquals("n1", beans.get(0).stringField);
        assertEquals("n2", beans.get(1).stringField);
    }

    @Test
    public void optionalObject() {
        Optional<Bean> parsedBean = JSON.fromJSON(Types.optional(Bean.class), JSON.toJSON(Optional.empty()));
        assertFalse(parsedBean.isPresent());

        parsedBean = JSON.fromJSON(Types.optional(Bean.class), JSON.toJSON(null));
        assertFalse(parsedBean.isPresent());

        Bean bean = new Bean();
        bean.stringField = "name";
        parsedBean = JSON.fromJSON(Types.optional(Bean.class), JSON.toJSON(Optional.of(bean)));
        assertTrue(parsedBean.isPresent());
        assertEquals(bean.stringField, parsedBean.get().stringField);
    }

    @Test
    public void nullObject() {
        String json = JSON.toJSON(null);
        Bean bean = JSON.fromJSON(Bean.class, json);

        assertNull(bean);
    }

    @Test
    public void notAnnotatedField() {
        Bean bean = new Bean();
        bean.notAnnotatedField = 100;
        String json = JSON.toJSON(bean);
        assertThat(json, containsString("\"notAnnotatedField\":100"));

        Bean parsedBean = JSON.fromJSON(Bean.class, json);
        assertEquals(bean.notAnnotatedField, parsedBean.notAnnotatedField);
    }

    @Test
    public void enumValue() {
        assertEquals(TestEnum.A, JSON.fromEnumValue(TestEnum.class, "A1"));
        assertEquals("B1", JSON.toEnumValue(TestEnum.B));
    }

    enum TestEnum {
        @Property(name = "A1")
        A,
        @Property(name = "B1")
        B
    }

    static class Child {
        @Property(name = "boolean")
        public Boolean booleanField;
    }

    static class Bean {
        @Property(name = "map")
        public final Map<String, String> mapField = Maps.newHashMap();

        @Property(name = "list")
        public final List<String> listField = Lists.newArrayList();

        @Property(name = "children")
        public final List<Child> childrenField = Lists.newArrayList();

        @Property(name = "string")
        public String stringField;

        @Property(name = "date")
        public LocalDate dateField;

        @Property(name = "date_time")
        public LocalDateTime dateTimeField;

        @Property(name = "instant")
        public Instant instantField;

        @Property(name = "zonedDateTime")
        public ZonedDateTime zonedDateTimeField;

        public Integer notAnnotatedField;
    }
}
