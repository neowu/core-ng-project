package core.framework.search.impl;

import core.framework.util.Strings;
import org.apache.http.entity.ByteArrayEntity;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class ElasticSearchLogInterceptorTest {
    @Test
    void bodyParam() {
        var param = new ElasticSearchLogInterceptor.BodyParam(new ByteArrayEntity(Strings.bytes("1234567890".repeat(3))));
        var builder = new StringBuilder();
        param.append(builder, Set.of(), 5);
        assertThat(builder).hasToString("12345...(truncated)");

        builder = new StringBuilder();
        param.append(builder, Set.of(), 30);
        assertThat(builder).hasToString("123456789012345678901234567890");

        builder = new StringBuilder();
        param.append(builder, Set.of(), 50);
        assertThat(builder).hasToString("123456789012345678901234567890");
    }
}
