package core.framework.json;

import core.framework.util.Types;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author neo
 */
class JSONTest {
    @Test
    void mapField() {
        TestBean bean = new TestBean();
        bean.mapField.put("key1", "value1");
        bean.mapField.put("key2", "value2");

        String json = JSON.toJSON(bean);
        assertThat(json).contains("\"map\":{\"key1\":\"value1\",\"key2\":\"value2\"}");

        TestBean parsedBean = JSON.fromJSON(TestBean.class, json);
        assertEquals("value1", parsedBean.mapField.get("key1"));
        assertEquals("value2", parsedBean.mapField.get("key2"));
    }

    @Test
    void childField() {
        TestBean bean = new TestBean();

        TestBean.Child child = new TestBean.Child();
        child.booleanField = true;
        child.longField = 200L;
        bean.childField = child;

        String json = JSON.toJSON(bean);
        assertThat(json).contains("\"child\":{\"boolean\":true,\"long\":200}");

        TestBean parsedBean = JSON.fromJSON(TestBean.class, json);
        assertEquals(bean.childField.booleanField, parsedBean.childField.booleanField);
        assertEquals(bean.childField.longField, parsedBean.childField.longField);
    }

    @Test
    void listField() {
        TestBean bean = new TestBean();
        bean.listField.add("value1");
        bean.listField.add("value2");

        TestBean.Child child1 = new TestBean.Child();
        child1.booleanField = true;
        bean.childrenField.add(child1);
        TestBean.Child child2 = new TestBean.Child();
        child2.booleanField = false;
        bean.childrenField.add(child2);

        String json = JSON.toJSON(bean);
        assertThat(json).contains("\"list\":[\"value1\",\"value2\"],\"children\":[{\"boolean\":true,\"long\":null},{\"boolean\":false,\"long\":null}]");

        TestBean parsedBean = JSON.fromJSON(TestBean.class, json);
        assertEquals(bean.listField, parsedBean.listField);
        assertEquals(2, parsedBean.childrenField.size());
        assertTrue(parsedBean.childrenField.get(0).booleanField);
        assertFalse(parsedBean.childrenField.get(1).booleanField);
    }

    @Test
    void dateField() {
        TestBean bean = new TestBean();
        bean.instantField = Instant.now();
        bean.dateTimeField = LocalDateTime.ofInstant(bean.instantField, ZoneId.systemDefault());
        bean.dateField = bean.dateTimeField.toLocalDate();
        bean.zonedDateTimeField = ZonedDateTime.ofInstant(bean.instantField, ZoneId.systemDefault());

        String json = JSON.toJSON(bean);

        TestBean parsedBean = JSON.fromJSON(TestBean.class, json);
        assertEquals(bean.instantField, parsedBean.instantField);
        assertEquals(bean.dateField, parsedBean.dateField);
        assertEquals(bean.dateTimeField, parsedBean.dateTimeField);
        assertEquals(bean.zonedDateTimeField.toInstant(), parsedBean.zonedDateTimeField.toInstant());
    }

    @Test
    void listObject() {
        List<TestBean> beans = JSON.fromJSON(Types.list(TestBean.class), "[{\"string\":\"n1\"},{\"string\":\"n2\"}]");

        assertEquals(2, beans.size());
        assertEquals("n1", beans.get(0).stringField);
        assertEquals("n2", beans.get(1).stringField);
    }

    @Test
    void optionalObject() {
        Optional<TestBean> parsedBean = JSON.fromJSON(Types.optional(TestBean.class), JSON.toJSON(Optional.empty()));
        assertFalse(parsedBean.isPresent());

        parsedBean = JSON.fromJSON(Types.optional(TestBean.class), JSON.toJSON(null));
        assertFalse(parsedBean.isPresent());

        TestBean bean = new TestBean();
        bean.stringField = "name";
        parsedBean = JSON.fromJSON(Types.optional(TestBean.class), JSON.toJSON(Optional.of(bean)));
        assertTrue(parsedBean.isPresent());
        assertEquals(bean.stringField, parsedBean.get().stringField);
    }

    @Test
    void nullObject() {
        String json = JSON.toJSON(null);
        TestBean bean = JSON.fromJSON(TestBean.class, json);

        assertNull(bean);
    }

    @Test
    void notAnnotatedField() {
        TestBean bean = new TestBean();
        bean.notAnnotatedField = 100;
        String json = JSON.toJSON(bean);
        assertThat(json).contains("\"notAnnotatedField\":100");

        TestBean parsedBean = JSON.fromJSON(TestBean.class, json);
        assertEquals(bean.notAnnotatedField, parsedBean.notAnnotatedField);
    }

    @Test
    void enumValue() {
        assertEquals(TestBean.TestEnum.A, JSON.fromEnumValue(TestBean.TestEnum.class, "A1"));
        assertEquals(TestBean.TestEnum.C, JSON.fromEnumValue(TestBean.TestEnum.class, "C"));

        assertEquals("B1", JSON.toEnumValue(TestBean.TestEnum.B));
        assertEquals("C", JSON.toEnumValue(TestBean.TestEnum.C));
    }

    @Test
    void empty() {
        TestBean bean = new TestBean();
        bean.empty = new TestBean.Empty();
        String json = JSON.toJSON(bean);
        assertThat(json).contains("\"empty\":{}");

        TestBean parsedBean = JSON.fromJSON(TestBean.class, json);
        assertNotNull(parsedBean.empty);
    }
}
