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
        assertThat(parsedBean.mapField)
                .containsEntry("key1", "value1")
                .containsEntry("key2", "value2");
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
        assertThat(parsedBean).isEqualToComparingFieldByFieldRecursively(bean);
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
        assertThat(parsedBean).isEqualToComparingFieldByFieldRecursively(bean);
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
        assertThat(parsedBean).isEqualToComparingOnlyGivenFields(bean, "instantField", "dateTimeField", "dateField");
        assertThat(parsedBean.zonedDateTimeField).isEqualTo(bean.zonedDateTimeField);
    }

    @Test
    void dateFieldFromJavaScript() {    // JS always encodes Date type into ISO format
        TestBean bean = JSON.fromJSON(TestBean.class, "{\"date\": \"2018-05-10T05:42:09.776Z\", \"dateTime\": \"2018-05-10T05:42:09.776Z\", \"zonedDateTime\": \"2018-05-10T05:42:09.776Z\"}");

        assertThat(bean.dateField).isEqualTo("2018-05-10");
        assertThat(bean.dateTimeField).isEqualTo("2018-05-10T05:42:09.776");
        assertThat(bean.zonedDateTimeField).isEqualTo("2018-05-10T05:42:09.776Z");
    }

    @Test
    void listObject() {
        List<TestBean> beans = JSON.fromJSON(Types.list(TestBean.class), "[{\"string\":\"n1\"},{\"string\":\"n2\"}]");

        assertThat(beans).hasSize(2);
        assertThat(beans.get(0).stringField).isEqualTo("n1");
        assertThat(beans.get(1).stringField).isEqualTo("n2");
    }

    @Test
    void optionalObject() {
        Optional<TestBean> parsedBean = JSON.fromJSON(Types.optional(TestBean.class), JSON.toJSON(Optional.empty()));
        assertThat(parsedBean).isNotPresent();

        parsedBean = JSON.fromJSON(Types.optional(TestBean.class), JSON.toJSON(null));
        assertThat(parsedBean).isNotPresent();

        TestBean bean = new TestBean();
        bean.stringField = "name";
        parsedBean = JSON.fromJSON(Types.optional(TestBean.class), JSON.toJSON(Optional.of(bean)));
        assertThat(parsedBean).get().isEqualToComparingFieldByField(bean);
    }

    @Test
    void nullObject() {
        String json = JSON.toJSON(null);
        TestBean bean = JSON.fromJSON(TestBean.class, json);

        assertThat(bean).isNull();
    }

    @Test
    void notAnnotatedField() {
        TestBean bean = new TestBean();
        bean.notAnnotatedField = 100;
        String json = JSON.toJSON(bean);
        assertThat(json).contains("\"notAnnotatedField\":100");

        TestBean parsedBean = JSON.fromJSON(TestBean.class, json);
        assertThat(parsedBean).isEqualToComparingFieldByFieldRecursively(bean);
    }

    @Test
    void enumValue() {
        assertThat(JSON.fromEnumValue(TestBean.TestEnum.class, "A1")).isEqualTo(TestBean.TestEnum.A);
        assertThat(JSON.fromEnumValue(TestBean.TestEnum.class, "C")).isEqualTo(TestBean.TestEnum.C);

        assertThat(JSON.toEnumValue(TestBean.TestEnum.B)).isEqualTo("B1");
        assertThat(JSON.toEnumValue(TestBean.TestEnum.C)).isEqualTo("C");
    }

    @Test
    void empty() {
        TestBean bean = new TestBean();
        bean.empty = new TestBean.Empty();
        String json = JSON.toJSON(bean);
        assertThat(json).contains("\"empty\":{}");

        TestBean parsedBean = JSON.fromJSON(TestBean.class, json);
        assertThat(parsedBean.empty).isNotNull();
    }
}
