package core.framework.http;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class HTTPClientBuilderTest {
    private HTTPClientBuilder builder;

    @BeforeEach
    void createHTTPClientBuilder() {
        builder = new HTTPClientBuilder();
    }

    @Test
    void callTimeout() {
        builder.connectTimeout(Duration.ofSeconds(1));
        builder.timeout(Duration.ofSeconds(2));
        assertThat(builder.callTimeout()).isEqualTo(Duration.ofMillis(1000 + 2000 + 2000));

        builder.maxRetries(1);
        assertThat(builder.callTimeout()).isEqualTo(Duration.ofMillis(1000 + 2000 + 2000));

        builder.maxRetries(2);
        assertThat(builder.callTimeout()).isEqualTo(Duration.ofMillis(1000 + 2000 * 2 + 600 + 2000));

        builder.maxRetries(3);
        assertThat(builder.callTimeout()).isEqualTo(Duration.ofMillis(1000 + 2000 * 3 + 600 + 1200 + 2000));
    }
}
