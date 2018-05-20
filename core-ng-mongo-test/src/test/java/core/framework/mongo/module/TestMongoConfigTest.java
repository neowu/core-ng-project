package core.framework.mongo.module;

import core.framework.mongo.impl.MockMongo;
import core.framework.mongo.impl.MongoImpl;
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
    void createMongo() {
        MongoImpl mongo = config.createMongo();
        assertThat(mongo).isInstanceOf(MockMongo.class);
    }
}
