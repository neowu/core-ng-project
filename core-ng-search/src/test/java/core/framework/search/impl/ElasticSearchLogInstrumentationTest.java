package core.framework.search.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ElasticSearchLogInstrumentationTest {
    private ElasticSearchLogInstrumentation instrumentation;

    @BeforeEach
    void createElasticSearchLogInstrumentation() {
        instrumentation = new ElasticSearchLogInstrumentation();
    }

    @Test
    void readBody() {
        ByteBuffer buffer1 = ByteBuffer.wrap("buffer1".getBytes());
        ByteBuffer buffer2 = ByteBuffer.wrap(" buffer2".getBytes());
        List<ByteBuffer> buffers = List.of(buffer1, buffer2);

        byte[] result = instrumentation.readBody(buffers);

        assertThat(result).asString().isEqualTo("buffer1 buffer2");

        assertThat(buffer1.position()).isZero();
        assertThat(buffer2.position()).isZero();
    }
}
