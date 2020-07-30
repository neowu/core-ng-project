package core.framework.module;

import core.framework.internal.module.ModuleContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class DBConfigTest {
    private DBConfig config;

    @BeforeEach
    void createDBConfig() {
        config = new DBConfig();
        config.initialize(new ModuleContext(null), null);
    }

    @Test
    void validate() {
        assertThatThrownBy(() -> config.validate())
                .hasMessageContaining("db url must be configured");

        config.url("jdbc:hsqldb:mem:.");
        assertThatThrownBy(() -> config.validate())
                .hasMessageContaining("db is configured but no repository/view added");
    }
}
