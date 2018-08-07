package core.framework.impl.json;

import core.framework.api.json.Property;
import core.framework.util.Charsets;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class PartialJSONWriterTest {
    @Test
    void toJSON() {
        TestBean bean = new TestBean();

        PartialJSONWriter<Object> writer = PartialJSONWriter.of(TestBean.class);
        byte[] json = writer.toJSON(bean);

        assertThat(new String(json, Charsets.UTF_8)).isEqualTo("{\"defaultValue\":\"defaultValue\",\"child\":{}}");
    }

    public static class TestChildBean {
        @Property(name = "boolean")
        public Boolean booleanField;

        @Property(name = "long")
        public Long longField;
    }

    public class TestBean {
        @Property(name = "string")
        public String stringField;

        @Property(name = "date")
        public LocalDate dateField;

        @Property(name = "defaultValue")
        public String defaultValueField = "defaultValue";

        @Property(name = "child")
        public TestChildBean child = new TestChildBean();
    }
}
