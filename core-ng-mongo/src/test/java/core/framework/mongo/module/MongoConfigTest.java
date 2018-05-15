package core.framework.mongo.module;

import core.framework.test.inject.TestBeanFactory;
import core.framework.test.module.TestModuleContext;
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
        config = new MongoConfig(new TestModuleContext(new TestBeanFactory()), null);
    }

    @Test
    void validate() {
        assertThatThrownBy(() -> config.validate())
                .hasMessageContaining("mongo().uri() must be configured");

        config.uri("mongodb://uri/db");

        assertThatThrownBy(() -> config.validate())
                .hasMessageContaining("no collection/view added");
    }
}
