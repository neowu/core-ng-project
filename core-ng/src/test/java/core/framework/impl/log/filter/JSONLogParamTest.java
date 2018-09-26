package core.framework.impl.log.filter;

import core.framework.util.Strings;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class JSONLogParamTest {
    @Test
    void filterWithoutMask() {
        var param = new JSONLogParam(null, null);
        var value = "{\"field1\": \"value1\"}";
        assertThat(param.filter(value, Set.of()).toString()).isEqualTo(value);
    }

    @Test
    void filterWithOneMaskedField() {
        var param = new JSONLogParam(null, null);
        var value = "{\"field1\": \"value1\",\n  \"password\": \"pass123\",\n  \"field2\": \"value2\"\n}";
        assertThat(param.filter(value, Set.of("password", "passwordConfirm")).toString())
                .isEqualTo("{\"field1\": \"value1\",\n  \"password\": \"******\",\n  \"field2\": \"value2\"\n}");
    }

    @Test
    void filterWithMultipleMaskedFields() {
        var param = new JSONLogParam(null, null);
        var value = "{\"field1\": \"value1\",\n  \"password\": \"pass123\",\n  \"passwordConfirm\": \"pass123\",\n  \"field2\": \"value2\",\n  \"nested\": {\n    \"password\": \"pass\\\"123\",\n    \"passwordConfirm\": \"pass123\"}}";
        assertThat(param.filter(value, Set.of("password", "passwordConfirm")).toString())
                .isEqualTo("{\"field1\": \"value1\",\n  \"password\": \"******\",\n  \"passwordConfirm\": \"******\",\n  \"field2\": \"value2\",\n  \"nested\": {\n    \"password\": \"******\",\n    \"passwordConfirm\": \"******\"}}");
    }

    @Test
    void filterWithBrokenJSON() {
        var param = new JSONLogParam(null, null);
        var value = "{\"field1\": \"value1\",\n  \"password\": \"pass123\",\n  \"passwordConfirm\": \"pass12";
        assertThat(param.filter(value, Set.of("password", "passwordConfirm"))).doesNotContain("pass123");

        value = "{\"field1\": \"value1\",\n  \"password\": \"pass123\",\n  \"passwordConfirm\"";
        assertThat(param.filter(value, Set.of("password", "passwordConfirm"))).doesNotContain("pass123");
    }

    @Test
    void append() {
        var param = new JSONLogParam(Strings.bytes("message1234567890"), UTF_8);
        var builder = new StringBuilder();
        param.append(builder, Set.of(), 10);
        assertThat(builder.toString()).isEqualTo("message12...(truncated)");

        builder = new StringBuilder();
        param.append(builder, Set.of(), 1000);
        assertThat(builder.toString()).isEqualTo("message1234567890");
    }

    @Test
    void appendWithMask() {
        var param = new JSONLogParam(Strings.bytes("{\"field1\":\"value1\",\"password\":\"pass123\",\"field2\":\"value2\"}"), UTF_8);
        var builder = new StringBuilder();
        param.append(builder, Set.of("password", "passwordConfirm"), 50);
        assertThat(builder.toString())
                .isEqualTo("{\"field1\":\"value1\",\"password\":\"******\",\"field2\":\"...(truncated)");
    }
}
