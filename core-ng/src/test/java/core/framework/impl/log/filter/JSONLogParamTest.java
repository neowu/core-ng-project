package core.framework.impl.log.filter;

import core.framework.util.Strings;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class JSONLogParamTest {
    @Test
    void filterWithoutMasking() {
        String value = "{\"field1\": \"value1\"}";
        LogParam param = param(value);
        String message = param.filter(Set.of());
        assertThat(message).isEqualTo(value);
    }

    @Test
    void filterWithOneMaskedField() {
        String value = "{\"field1\": \"value1\",\n  \"password\": \"pass123\",\n  \"field2\": \"value2\"\n}";
        LogParam param = param(value);
        String message = param.filter(Set.of("password", "passwordConfirm"));
        assertThat(message).isEqualTo("{\"field1\": \"value1\",\n  \"password\": \"******\",\n  \"field2\": \"value2\"\n}");
    }

    @Test
    void filterWithMultipleMaskedFields() {
        String value = "{\"field1\": \"value1\",\n  \"password\": \"pass123\",\n  \"passwordConfirm\": \"pass123\",\n  \"field2\": \"value2\",\n  \"nested\": {\n    \"password\": \"pass\\\"123\",\n    \"passwordConfirm\": \"pass123\"}}";
        LogParam param = param(value);
        String message = param.filter(Set.of("password", "passwordConfirm"));
        assertThat(message).isEqualTo("{\"field1\": \"value1\",\n  \"password\": \"******\",\n  \"passwordConfirm\": \"******\",\n  \"field2\": \"value2\",\n  \"nested\": {\n    \"password\": \"******\",\n    \"passwordConfirm\": \"******\"}}");
    }

    @Test
    void filterWithBrokenJSONMessage() {
        String value = "{\"field1\": \"value1\",\n  \"password\": \"pass123\",\n  \"passwordConfirm\": \"pass12";
        LogParam param = param(value);
        String message = param.filter(Set.of("password", "passwordConfirm"));
        assertThat(message).doesNotContain("pass123");

        value = "{\"field1\": \"value1\",\n  \"password\": \"pass123\",\n  \"passwordConfirm\"";
        param = param(value);
        message = param.filter(Set.of("password", "passwordConfirm"));
        assertThat(message).doesNotContain("pass123");
    }

    private LogParam param(String value) {
        return new JSONLogParam(Strings.bytes(value), StandardCharsets.UTF_8);
    }
}
