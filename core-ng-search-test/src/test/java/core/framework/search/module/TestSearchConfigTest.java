package core.framework.search.module;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    void auth() {
        config.auth("id", "secret");
    }
}
