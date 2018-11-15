package core.framework.mongo.module;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class TestMongoConfigTest {
    private TestMongoConfig config;

    @BeforeEach
    void createTestMongoConfig() {
        config = new TestMongoConfig();
    }

    @Test
    void connectionString() {
        assertThat(config.connectionString(null).getDatabase()).isNotNull();
    }
}
