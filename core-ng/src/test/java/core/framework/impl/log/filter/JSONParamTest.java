package core.framework.impl.log.filter;

import core.framework.util.Charsets;
import core.framework.util.Sets;
import core.framework.util.Strings;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class JSONParamTest {
    @Test
    void filterWithoutMasking() {
        String value = "{\"field1\": \"value1\"}";
        FilterParam param = param(value);
        String message = param.filter(Sets.newHashSet());
        assertThat(message).isEqualTo(value);
    }

    @Test
    void filterWithOneMaskedField() {
        String value = "{\"field1\": \"value1\",\n  \"password\": \"pass123\",\n  \"field2\": \"value2\"\n}";
        FilterParam param = param(value);
        String message = param.filter(Sets.newHashSet("password", "passwordConfirm"));
        assertThat(message).isEqualTo("{\"field1\": \"value1\",\n  \"password\": \"******\",\n  \"field2\": \"value2\"\n}");
    }

    @Test
    void filterWithMultipleMaskedFields() {
        String value = "{\"field1\": \"value1\",\n  \"password\": \"pass123\",\n  \"passwordConfirm\": \"pass123\",\n  \"field2\": \"value2\",\n  \"nested\": {\n    \"password\": \"pass\\\"123\",\n    \"passwordConfirm\": \"pass123\"}}";
        FilterParam param = param(value);
        String message = param.filter(Sets.newHashSet("password", "passwordConfirm"));
        assertThat(message).isEqualTo("{\"field1\": \"value1\",\n  \"password\": \"******\",\n  \"passwordConfirm\": \"******\",\n  \"field2\": \"value2\",\n  \"nested\": {\n    \"password\": \"******\",\n    \"passwordConfirm\": \"******\"}}");
    }

    @Test
    void filterWithBrokenJSONMessage() {
        String value = "{\"field1\": \"value1\",\n  \"password\": \"pass123\",\n  \"passwordConfirm\": \"pass12";
        FilterParam param = param(value);
        String message = param.filter(Sets.newHashSet("password", "passwordConfirm"));
        assertThat(message).doesNotContain("pass123");

        value = "{\"field1\": \"value1\",\n  \"password\": \"pass123\",\n  \"passwordConfirm\"";
        param = param(value);
        message = param.filter(Sets.newHashSet("password", "passwordConfirm"));
        assertThat(message).doesNotContain("pass123");
    }

    private FilterParam param(String value) {
        return new JSONParam(Strings.bytes(value), Charsets.UTF_8);
    }
}
