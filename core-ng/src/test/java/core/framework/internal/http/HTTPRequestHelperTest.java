package core.framework.internal.http;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class HTTPRequestHelperTest {
    @Test
    void encodeWithEmptyValue() {
        var builder = new StringBuilder();
        Map<String, String> params = new LinkedHashMap<>();
        params.put("key1", "");
        params.put("key2", "value2");
        params.put("key3", null);   // both null and empty will be serialized as empty
        HTTPRequestHelper.urlEncoding(builder, params);
        assertThat(builder.toString()).isEqualTo("key1=&key2=value2&key3=");
    }

    @Test
    void encode() {
        var builder = new StringBuilder();
        Map<String, String> params = new LinkedHashMap<>();
        params.put("key1", "value1");
        params.put("key2", "value2");
        HTTPRequestHelper.urlEncoding(builder, params);
        assertThat(builder.toString()).isEqualTo("key1=value1&key2=value2");
    }
}
