package core.framework.module;

import core.framework.impl.db.Vendor;
import core.framework.impl.inject.BeanFactory;
import core.framework.test.module.TestModuleContext;
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
        TestModuleContext context = new TestModuleContext(new BeanFactory());
        config = new TestDBConfig(context, null);
    }

    @Test
    void databaseURL() {
        assertThat(config.databaseURL(null, Vendor.MYSQL))
                .isEqualTo("jdbc:hsqldb:mem:.;sql.syntax_mys=true");

        assertThat(config.databaseURL(null, Vendor.ORACLE))
                .isEqualTo("jdbc:hsqldb:mem:.;sql.syntax_ora=true");
    }
}
