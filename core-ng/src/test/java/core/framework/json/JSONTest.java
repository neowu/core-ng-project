package core.framework.json;

import core.framework.util.Types;
import org.junit.jupiter.api.Test;

import java.io.UncheckedIOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class JSONTest {
    @Test
    void mapField() {
        var bean = new TestBean();
        bean.mapField.put("key1", "value1");
        bean.mapField.put("key2", "value2");
        bean.enumMapField.put(TestBean.TestEnum.A, "A1");
        bean.enumMapField.put(TestBean.TestEnum.B, "B1");
        bean.mapListField.put("key1", List.of("v1"));

        String json = JSON.toJSON(bean);
        assertThat(json)
            .contains("\"map\":{\"key1\":\"value1\",\"key2\":\"value2\"}")
            .contains("\"enumMap\":{\"A1\":\"A1\",\"B1\":\"B1\"}")
            .contains("\"listMap\":{\"key1\":[\"v1\"]}");

        var parsedBean = JSON.fromJSON(TestBean.class, json);
        assertThat(parsedBean).usingRecursiveComparison().isEqualTo(bean);
    }

    @Test
    void childField() {
        var bean = new TestBean();

        var child = new TestBean.Child();
        child.booleanField = Boolean.TRUE;
        child.longField = 200L;
        child.doubleField = 2.3456;
        bean.childField = child;

        String json = JSON.toJSON(bean);
        assertThat(json).contains("""
            "child":{"boolean":true,"long":200,"double":2.3456}""");

        var parsedBean = JSON.fromJSON(TestBean.class, json);
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

        String json = JSON.toJSON(bean);
        assertThat(json).contains("""
            "list":["value1","value2"],"children":[{"boolean":true,"long":null,"double":null},{"boolean":false,"long":null,"double":null}]""");

        TestBean parsedBean = JSON.fromJSON(TestBean.class, json);
        assertThat(parsedBean).usingRecursiveComparison().isEqualTo(bean);
    }

    @Test
    void dateField() {
        var bean = new TestBean();
        bean.instantField = Instant.now();
        bean.dateTimeField = LocalDateTime.ofInstant(bean.instantField, ZoneId.systemDefault());
        bean.dateField = bean.dateTimeField.toLocalDate();
        bean.zonedDateTimeField = ZonedDateTime.ofInstant(bean.instantField, ZoneId.systemDefault());
        bean.timeField = LocalTime.ofInstant(bean.instantField, ZoneId.systemDefault());
        String json = JSON.toJSON(bean);

        TestBean parsedBean = JSON.fromJSON(TestBean.class, json);
        assertThat(parsedBean).usingRecursiveComparison()
            .withComparatorForType(ChronoZonedDateTime.timeLineOrder(), ZonedDateTime.class)
            .isEqualTo(bean);
    }

    @Test
    void dateFieldFromJavaScript() {    // JS always encodes Date type into ISO format
        TestBean bean = JSON.fromJSON(TestBean.class, "{\"date\": \"2018-05-10T05:42:09.776Z\", \"dateTime\": \"2018-05-10T05:42:09.776Z\", \"zonedDateTime\": \"2018-05-10T05:42:09.776Z\"}");

        assertThat(bean.dateField).isEqualTo("2018-05-10");
        assertThat(bean.dateTimeField).isEqualTo("2018-05-10T05:42:09.776");
        assertThat(bean.zonedDateTimeField).isEqualTo("2018-05-10T05:42:09.776Z");
    }

    @Test
    void nanoFractionOfDateField() {
        assertThat(JSON.toJSON(LocalDateTime.of(2019, 4, 25, 1, 0, 0, 200000000)))
            .isEqualTo("\"2019-04-25T01:00:00.200\"");

        assertThat(JSON.toJSON(LocalDateTime.of(2019, 4, 25, 1, 0, 0, 0)))
            .isEqualTo("\"2019-04-25T01:00:00.000\"");

        assertThat(JSON.toJSON(ZonedDateTime.of(2019, 4, 25, 1, 0, 0, 200000000, ZoneId.of("UTC"))))
            .isEqualTo("\"2019-04-25T01:00:00.200Z\"");

        assertThat(JSON.toJSON(ZonedDateTime.of(2019, 4, 25, 1, 0, 0, 0, ZoneId.of("UTC"))))
            .isEqualTo("\"2019-04-25T01:00:00Z\"");

        assertThat(JSON.toJSON(ZonedDateTime.of(2019, 4, 25, 1, 0, 0, 0, ZoneId.of("America/New_York"))))
            .isEqualTo("\"2019-04-25T05:00:00Z\"");  // New york is UTC+5

        assertThat(JSON.toJSON(LocalTime.of(18, 0)))
            .isEqualTo("\"18:00:00.000\"");

        assertThat(JSON.toJSON(LocalTime.of(18, 1, 2, 200000000)))
            .isEqualTo("\"18:01:02.200\"");

        assertThat(JSON.toJSON(LocalTime.of(18, 1, 2, 123456789)))
            .isEqualTo("\"18:01:02.123456789\"");
    }

    @Test
    void enumField() {
        var bean = new TestBean();
        bean.enumField = TestBean.TestEnum.C;

        String json = JSON.toJSON(bean);
        TestBean parsedBean = JSON.fromJSON(TestBean.class, json);

        assertThat(parsedBean.enumField).isEqualTo(bean.enumField);
    }

    @Test
    void listObject() {
        List<TestBean> beans = JSON.fromJSON(Types.list(TestBean.class), "[{\"string\":\"n1\"},{\"string\":\"n2\"}]");

        assertThat(beans).hasSize(2);
        assertThat(beans.get(0).stringField).isEqualTo("n1");
        assertThat(beans.get(1).stringField).isEqualTo("n2");
    }

    @Test
    void nullObject() {
        assertThatThrownBy(() -> JSON.fromJSON(TestBean.class, "null"))
            .isInstanceOf(Error.class)
            .hasMessageContaining("invalid json");

        assertThatThrownBy(() -> JSON.toJSON(null))
            .isInstanceOf(Error.class)
            .hasMessageContaining("instance must not be null");
    }

    @Test
    void enumValue() {
        assertThat(JSON.fromEnumValue(TestBean.TestEnum.class, "A1")).isEqualTo(TestBean.TestEnum.A);
        assertThat(JSON.fromEnumValue(TestBean.TestEnum.class, "C")).isEqualTo(TestBean.TestEnum.C);

        assertThat(JSON.toEnumValue(TestBean.TestEnum.B)).isEqualTo("B1");
        assertThat(JSON.toEnumValue(TestBean.TestEnum.C)).isEqualTo("C");

        // ordinal should be treated as invalid value
        assertThatThrownBy(() -> JSON.fromEnumValue(TestBean.TestEnum.class, "0"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void empty() {
        var bean = new TestBean();
        bean.empty = new TestBean.Empty();
        String json = JSON.toJSON(bean);
        assertThat(json).contains("\"empty\":{}");

        TestBean parsedBean = JSON.fromJSON(TestBean.class, json);
        assertThat(parsedBean.empty).isNotNull();
    }

    @Test
    void defaultValue() {
        TestBean bean = JSON.fromJSON(TestBean.class, "{}");

        assertThat(bean.defaultValueField).isEqualTo("defaultValue");
    }

    @Test
    void invalidJSON() {
        assertThatThrownBy(() -> JSON.fromJSON(TestBean.class, "{"))
            .isInstanceOf(UncheckedIOException.class);

        assertThatThrownBy(() -> JSON.fromJSON(Types.list(TestBean.class), "{"))
            .isInstanceOf(UncheckedIOException.class);
    }

    @Test
    void invalidInteger() {
        assertThatThrownBy(() -> JSON.fromJSON(Integer.class, "\"\""))
            .isInstanceOf(UncheckedIOException.class);
    }
}
