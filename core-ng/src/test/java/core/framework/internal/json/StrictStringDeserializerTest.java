package core.framework.internal.json;

import core.framework.json.JSONException;
import core.framework.json.TestBean;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class StrictStringDeserializerTest {
    private final JSONReader<TestBean> reader = new JSONReader<>(TestBean.class);

    @Test
    void validate() {
        assertThat(StrictStringDeserializer.validate("")).isNull();
        assertThat(StrictStringDeserializer.validate("value\t\n\r")).isNull();
        assertThat(StrictStringDeserializer.validate("中文😀")).isNull();

        assertThat(StrictStringDeserializer.validate("value\u0000")).contains("NUL").contains("index=5");
        assertThat(StrictStringDeserializer.validate("\uD800")).contains("lone surrogate");
        assertThat(StrictStringDeserializer.validate("\uD800a")).contains("lone surrogate");
        assertThat(StrictStringDeserializer.validate("a\uDC00")).contains("lone surrogate").contains("index=1");
        assertThat(StrictStringDeserializer.validate("\uDE00\uD83D")).contains("lone surrogate").contains("index=0");
    }

    @Test
    void fromJSON() {
        TestBean bean = reader.fromJSON("""
            {"string":"value \\uD83D\\uDE00","list":["中文"],"map":{"key":"value"}}""".getBytes(UTF_8));
        assertThat(bean.stringField).isEqualTo("value 😀");
        assertThat(bean.listField).containsExactly("中文");
        assertThat(bean.mapField).containsEntry("key", "value");
    }

    @Test
    void rejectInvalidString() {
        assertInvalid("{\"string\":\"www.example.com\\u0000\"}");
        assertInvalid("{\"string\":\"\\uD800\"}");
        assertInvalid("{\"list\":[\"\\u0000\"]}");
        assertInvalid("{\"map\":{\"key\":\"\\uDC00\"}}");
        assertInvalid("{\"map\":{\"key\\u0000\":\"value\"}}");
        assertInvalid("{\"listMap\":{\"key\":[\"\\u0000\"]}}");
        assertInvalid("{\"defaultValue\":\"\\uD83D\"}");
    }

    private void assertInvalid(String json) {
        assertThatThrownBy(() -> reader.fromJSON(json.getBytes(UTF_8)))
            .isInstanceOf(JSONException.class);
    }
}
