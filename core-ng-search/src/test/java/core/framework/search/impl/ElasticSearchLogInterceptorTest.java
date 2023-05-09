package core.framework.search.impl;

import core.framework.util.Strings;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.InputStreamEntity;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
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

    @Test
    void bodyParamWithChunkedEntity() {
        var param = new ElasticSearchLogInterceptor.BodyParam(chunkedEntity("1234567890"));
        var builder = new StringBuilder();
        param.append(builder, Set.of(), 5);
        assertThat(builder).hasToString("12345...(truncated)");

        param = new ElasticSearchLogInterceptor.BodyParam(chunkedEntity("1234567890"));
        builder = new StringBuilder();
        param.append(builder, Set.of(), 10);
        assertThat(builder).hasToString("1234567890");

        param = new ElasticSearchLogInterceptor.BodyParam(chunkedEntity("1234567890"));
        builder = new StringBuilder();
        param.append(builder, Set.of(), 20);
        assertThat(builder).hasToString("1234567890");
    }

    InputStreamEntity chunkedEntity(String text) {
        var entity = new InputStreamEntity(new ByteArrayInputStream(Strings.bytes(text)));
        entity.setChunked(true);
        assertThat(entity.getContentLength()).isEqualTo(-1);
        return entity;
    }
}
