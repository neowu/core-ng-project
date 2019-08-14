package core.framework.module;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class TestDBConfigTest {
    private TestDBConfig config;

    @BeforeEach
    void createTestDBConfig() {
        config = new TestDBConfig();
    }

    @Test
    void databaseURL() {
        assertThat(config.databaseURL(null))
            .isEqualTo("jdbc:hsqldb:mem:.;sql.syntax_mys=true");

        config.name = "db1";
        assertThat(config.databaseURL(null))
            .isEqualTo("jdbc:hsqldb:mem:db1;sql.syntax_mys=true");
    }
}
