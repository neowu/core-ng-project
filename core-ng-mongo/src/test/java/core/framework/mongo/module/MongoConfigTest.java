package core.framework.mongo.module;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
}
