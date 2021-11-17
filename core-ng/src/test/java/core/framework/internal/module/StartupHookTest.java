package core.framework.internal.module;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class StartupHookTest {
    private StartupHook startupHook;

    @BeforeEach
    void createStartupHook() {
        startupHook = new StartupHook();
    }

    @Test
    void initialize() throws Exception {
        startupHook.initialize.add(() -> {
        });
        startupHook.initialize();

        assertThat(startupHook.initialize).isNull();
    }

    @Test
    void start() throws Exception {
        startupHook.start.add(() -> {
        });
        startupHook.start();

        assertThat(startupHook.start).isNull();
    }
}
