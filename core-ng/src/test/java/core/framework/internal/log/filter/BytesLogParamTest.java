package core.framework.internal.log.filter;

import core.framework.util.Strings;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class BytesLogParamTest {
    @Test
    void appendWithTruncation() {
        var param = new BytesLogParam(Strings.bytes("text-♥"));
        var builder = new StringBuilder();
        param.append(builder, Set.of(), 6);

        assertThat(builder.toString()).isEqualTo("text-...(truncated)");
    }

    @Test
    void appendNull() {
        var param = new BytesLogParam(null);
        var builder = new StringBuilder();
        param.append(builder, Set.of(), 6);

        assertThat(builder.toString()).isEqualTo("null");
    }

    @Test
    void append() {
        var param = new BytesLogParam(Strings.bytes("message1234567890"));
        var builder = new StringBuilder();
        param.append(builder, Set.of(), 10);
        assertThat(builder.toString()).isEqualTo("message12...(truncated)");

        builder = new StringBuilder();
        param.append(builder, Set.of(), 1000);
        assertThat(builder.toString()).isEqualTo("message1234567890");
    }

    @Test
    void appendWithMask() {
        var param = new BytesLogParam(Strings.bytes("{\"field1\":\"value1\",\"password\":\"pass123\",\"field2\":\"value2\"}"));
        var builder = new StringBuilder();
        param.append(builder, Set.of("password", "passwordConfirm"), 50);
        assertThat(builder.toString())
            .isEqualTo("{\"field1\":\"value1\",\"password\":\"******\",\"field2\":\"...(truncated)");
    }

    @Test
    void filterJSONWithoutMask() {
        var param = new BytesLogParam(null, null);
        var value = "{\"field1\": \"value1\"}";
        assertThat(param.filter(value, Set.of()).toString()).isEqualTo(value);
    }

    @Test
    void filterJSONWithOneMaskField() {
        var param = new BytesLogParam(null, null);
        // language=json
        var value = "{\"field1\": \"value1\",\n  \"password\" : \"pass123\",\n  \"field2\": \"value2\"\n}";
        assertThat(param.filter(value, Set.of("password", "passwordConfirm")).toString())
            .isEqualTo("{\"field1\": \"value1\",\n  \"password\" : \"******\",\n  \"field2\": \"value2\"\n}");

        value = "{\"field1\": \"value1\", \"password\": null, \"field2\": null, \"field3\": null}";
        assertThat(param.filter(value, Set.of("password", "passwordConfirm")).toString())
            .isEqualTo(value);

        value = "{\"field1\": \"value1\", \"password\": {\"field2\": null}}";
        assertThat(param.filter(value, Set.of("password", "passwordConfirm")).toString())
            .isEqualTo(value);

        value = "{\"field1\": [\"secret1\", \"secret2\", \"secret3\"], \"field2\": \"value\"}";
        assertThat(param.filter(value, Set.of("secret1", "secret2", "secret3")).toString())
            .isEqualTo(value);
    }

    @Test
    void filterJSONWithNull() {
        var param = new BytesLogParam(null, null);
        // language=json
        var value = "{\"field1\": \"value1\", \"password\": null, \"field2\": \"value2\"}";
        assertThat(param.filter(value, Set.of("password", "passwordConfirm")).toString())
            .isEqualTo("{\"field1\": \"value1\", \"password\": null, \"field2\": \"value2\"}");
    }

    @Test
    void filterJSONWithMultipleMaskFields() {
        var param = new BytesLogParam(null, null);
        var value = "{\"field1\": \"value1\", \"password\": \"pass123\", \"passwordConfirm\": \"pass123\", \"field2\": \"value2\", \"nested\": {\"password\": \"pass\\\"123\", \"passwordConfirm\": \"pass123\"}}";
        assertThat(param.filter(value, Set.of("password", "passwordConfirm")).toString())
            .isEqualTo("{\"field1\": \"value1\", \"password\": \"******\", \"passwordConfirm\": \"******\", \"field2\": \"value2\", \"nested\": {\"password\": \"******\", \"passwordConfirm\": \"******\"}}");
    }

    @Test
    void filterWithBrokenJSON() {
        var param = new BytesLogParam(null, null);
        var value = "{\"field1\": \"value1\",\n  \"password\": \"pass123\",\n  \"passwordConfirm\": \"pass12";
        assertThat(param.filter(value, Set.of("password", "passwordConfirm"))).doesNotContain("pass123");

        value = "{\"field1\": \"value1\",\n  \"password\": \"pass123\",\n  \"passwordConfirm\"";
        assertThat(param.filter(value, Set.of("password", "passwordConfirm"))).doesNotContain("pass123");

        value = "{\"field1\": \"value1\", \"password\": \"pass123";
        assertThat(param.filter(value, Set.of("password", "passwordConfirm")).toString())
            .isEqualTo("{\"field1\": \"value1\", \"password\": \"******");
    }
}
