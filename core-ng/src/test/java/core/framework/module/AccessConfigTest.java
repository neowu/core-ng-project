package core.framework.module;

import core.framework.internal.module.ModuleContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class AccessConfigTest {
    private AccessConfig config;

    @BeforeEach
    void createAccessConfig() {
        config = new AccessConfig(new ModuleContext(null));
    }

    @Test
    void cidrsLogParam() {
        assertThat(config.cidrsLogParam(List.of(), 2)).isEqualTo("[]");
        assertThat(config.cidrsLogParam(List.of("1.1.1.1/24", "2.1.1.1/24"), 2)).isEqualTo("[1.1.1.1/24, 2.1.1.1/24]");
        assertThat(config.cidrsLogParam(List.of("1.1.1.1/24", "2.1.1.1/24", "3.1.1.1/24"), 2)).isEqualTo("[1.1.1.1/24, 2.1.1.1/24, ...]");
    }

    @Test
    void config() {
        config.allow(List.of());
        assertThatThrownBy(() -> config.allow(List.of()))
                .isInstanceOf(Error.class)
                .hasMessageContaining("cidrs is already configured");

        config.deny(List.of());
        assertThatThrownBy(() -> config.deny(List.of()))
                .isInstanceOf(Error.class)
                .hasMessageContaining("cidrs is already configured");
    }
}
