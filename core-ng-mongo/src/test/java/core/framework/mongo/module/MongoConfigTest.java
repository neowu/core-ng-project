package core.framework.mongo.module;

import com.mongodb.ConnectionString;
import core.framework.internal.module.ReadinessProbe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class MongoConfigTest {
    private MongoConfig config;

    @BeforeEach
    void createMongoConfig() {
        config = new MongoConfig();
    }

    @Test
    void validate() {
        assertThatThrownBy(() -> config.validate())
            .hasMessageContaining("mongo uri must be configured");

        config.uri = "mongodb://uri/db";

        assertThatThrownBy(() -> config.validate())
            .hasMessageContaining("no collection/view added");
    }

    @Test
    void uri() {
        assertThatThrownBy(() -> config.uri("mongodb://localhost"))
            .hasMessageContaining("uri must have database");
    }

    @Test
    void addProbe() {
        var probe = new ReadinessProbe();
        config.addProbe(probe, new ConnectionString("mongodb+srv://server.example.com/db"));
        assertThat(probe.hostURIs).isEmpty();

        config.addProbe(probe, new ConnectionString("mongodb://server.example.com/db"));
        assertThat(probe.hostURIs).hasSize(1);
    }
}
