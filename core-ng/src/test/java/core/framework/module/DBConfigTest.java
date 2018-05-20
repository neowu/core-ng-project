package core.framework.module;

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
    }

    @Test
    void validate() {
        assertThatThrownBy(() -> config.validate())
                .hasMessageContaining("db url must be configured");
    }
}
