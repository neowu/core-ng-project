package core.framework.search.module;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class TestSearchConfigTest {
    private TestSearchConfig config;

    @BeforeEach
    void createTestSearchConfig() {
        config = new TestSearchConfig();
    }

    @Test
    void configureLogger() {
        config.configureLogger();

        assertThat(System.getProperty("log4j.configurationFactory")).isNotNull();
    }
}
