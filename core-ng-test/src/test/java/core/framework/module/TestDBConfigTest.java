package core.framework.module;

import core.framework.impl.db.Vendor;
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
        assertThat(config.databaseURL(null, Vendor.MYSQL))
                .isEqualTo("jdbc:hsqldb:mem:.;sql.syntax_mys=true");

        assertThat(config.databaseURL(null, Vendor.ORACLE))
                .isEqualTo("jdbc:hsqldb:mem:.;sql.syntax_ora=true");
    }
}
