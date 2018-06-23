package core.framework.impl.module;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author neo
 */
class ShutdownHookTest {
    private ShutdownHook shutdownHook;

    @BeforeEach
    void createShutdownHook() {
        shutdownHook = new ShutdownHook();
    }

    @Test
    void run() throws Exception {
        ShutdownHook.Shutdown shutdown1 = timeoutIn -> {
            throw new Error("failed to shutdown");
        };
        ShutdownHook.Shutdown shutdown2 = mock(ShutdownHook.Shutdown.class);
        shutdownHook.add(ShutdownHook.STAGE_0, shutdown1);
        shutdownHook.add(ShutdownHook.STAGE_10, shutdown2);

        shutdownHook.run();

        verify(shutdown2).execute(anyLong());
    }
}
