package app.monitor.job;

import core.framework.util.ClasspathResources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class ElasticSearchClientTest {
    private ElasticSearchClient client;

    @BeforeEach
    void createElasticSearchClient() {
        client = new ElasticSearchClient();
    }

    @Test
    void parseResponse() throws IOException {
        ElasticSearchNodeStats stats = client.parseResponse(ClasspathResources.bytes("es-job-test/stats.json"));
        assertThat(stats).isNotNull();

        assertThatThrownBy(() -> client.parseResponse(ClasspathResources.bytes("es-job-test/error.json")))
            .isInstanceOf(Error.class)
            .hasMessageStartingWith("failed to call elasticsearch node stats api, error=");
    }
}
