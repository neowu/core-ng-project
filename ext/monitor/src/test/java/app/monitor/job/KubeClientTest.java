package app.monitor.job;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class KubeClientTest {
    private KubeClient client;

    @BeforeEach
    void createKubeClient() {
        client = new KubeClient();
    }

    @Test
    void token() {
        client.lastUpdateTime = Instant.parse("2024-01-01T00:00:00Z");
        client.token = "old_value";
        assertThat(client.token(Instant.parse("2024-01-01T00:10:00Z"))).isEqualTo("old_value");
    }
}
