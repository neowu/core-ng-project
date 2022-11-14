package core.framework.json;

import core.framework.internal.validate.ValidationException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class BeanTest {
    @BeforeAll
    static void registerBean() {
        Bean.register(TestBean.class);
    }

    @Test
    void registerDuplicateBean() {
        assertThatThrownBy(() -> Bean.register(TestBean.class))
                .isInstanceOf(Error.class)
                .hasMessageContaining("bean class is already registered");
    }

    @Test
    void mapField() {
        var bean = new TestBean();
        bean.mapField.put("key1", "value1");
        bean.mapField.put("key2", "value2");
        bean.enumMapField.put(TestBean.TestEnum.A, "A1");
        bean.enumMapField.put(TestBean.TestEnum.B, "B1");

        String json = Bean.toJSON(bean);
        assertThat(json)
                .contains("\"map\":{\"key1\":\"value1\",\"key2\":\"value2\"}")
                .contains("\"enumMap\":{\"A1\":\"A1\",\"B1\":\"B1\"}");

        var parsedBean = Bean.fromJSON(TestBean.class, json);
        assertThat(parsedBean.mapField)
                .containsEntry("key1", "value1")
                .containsEntry("key2", "value2");

        assertThat(parsedBean.enumMapField)
                .containsEntry(TestBean.TestEnum.A, "A1")
                .containsEntry(TestBean.TestEnum.B, "B1");
    }

    @Test
    void childField() {
        var bean = new TestBean();

        var child = new TestBean.Child();
        child.booleanField = Boolean.TRUE;
        child.longField = 200L;
        bean.childField = child;

        String json = Bean.toJSON(bean);
        assertThat(json).contains("""
            "child":{"boolean":true,"long":200,"double":null}""");

        var parsedBean = Bean.fromJSON(TestBean.class, json);
        assertThat(parsedBean).usingRecursiveComparison().isEqualTo(bean);
    }

    @Test
    void listField() {
        var bean = new TestBean();
        bean.listField.add("value1");
        bean.listField.add("value2");

        var child1 = new TestBean.Child();
        child1.booleanField = Boolean.TRUE;
        bean.childrenField.add(child1);
        TestBean.Child child2 = new TestBean.Child();
        child2.booleanField = Boolean.FALSE;
        bean.childrenField.add(child2);

        String json = Bean.toJSON(bean);
        assertThat(json).contains("""
            "list":["value1","value2"],"children":[{"boolean":true,"long":null,"double":null},{"boolean":false,"long":null,"double":null}]""");

        TestBean parsedBean = Bean.fromJSON(TestBean.class, json);
        assertThat(parsedBean).usingRecursiveComparison().isEqualTo(bean);
    }

    @Test
    void dateField() {
        var bean = new TestBean();
        bean.instantField = Instant.now();
        bean.dateTimeField = LocalDateTime.ofInstant(bean.instantField, ZoneId.systemDefault());
        bean.dateField = bean.dateTimeField.toLocalDate();
        bean.zonedDateTimeField = ZonedDateTime.ofInstant(bean.instantField, ZoneId.systemDefault());
        String json = Bean.toJSON(bean);

        TestBean parsedBean = Bean.fromJSON(TestBean.class, json);
        assertThat(parsedBean).usingRecursiveComparison()
                              .withComparatorForType(ChronoZonedDateTime.timeLineOrder(), ZonedDateTime.class)
                              .isEqualTo(bean);
    }

    @Test
    void dateFieldFromJavaScript() {    // JS always encodes Date type into ISO format
        TestBean bean = Bean.fromJSON(TestBean.class, "{\"date\": \"2018-05-10T05:42:09.776Z\", \"dateTime\": \"2018-05-10T05:42:09.776Z\", \"zonedDateTime\": \"2018-05-10T05:42:09.776Z\"}");

        assertThat(bean.dateField).isEqualTo("2018-05-10");
        assertThat(bean.dateTimeField).isEqualTo("2018-05-10T05:42:09.776");
        assertThat(bean.zonedDateTimeField).isEqualTo("2018-05-10T05:42:09.776Z");
    }

    @Test
    void enumField() {
        var bean = new TestBean();
        bean.enumField = TestBean.TestEnum.C;

        String json = Bean.toJSON(bean);
        TestBean parsedBean = Bean.fromJSON(TestBean.class, json);

        assertThat(parsedBean.enumField).isEqualTo(bean.enumField);
    }

    @Test
    void empty() {
        var bean = new TestBean();
        bean.empty = new TestBean.Empty();
        String json = Bean.toJSON(bean);
        assertThat(json).contains("\"empty\":{}");

        TestBean parsedBean = Bean.fromJSON(TestBean.class, json);
        assertThat(parsedBean.empty).isNotNull();
    }

    @Test
    void defaultValue() {
        TestBean bean = Bean.fromJSON(TestBean.class, "{}");

        assertThat(bean.defaultValueField).isEqualTo("defaultValue");
    }

    @Test
    void validate() {
        var bean = new TestBean();
        bean.defaultValueField = null;

        assertThatThrownBy(() -> Bean.toJSON(bean))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("validation failed");

        assertThatThrownBy(() -> Bean.fromJSON(TestBean.class, "{\"defaultValue\": null}"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("validation failed");
    }
}
