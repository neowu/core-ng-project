package core.framework.impl.module;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
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

    @AfterEach
    void cleanup() {
        Runtime.getRuntime().removeShutdownHook(shutdownHook.thread);
    }

    @Test
    void run() throws Exception {
        var shutdown1 = mock(ShutdownHook.Shutdown.class);
        doThrow(new Error("failed to shutdown")).when(shutdown1).execute(anyLong());
        var shutdown2 = mock(ShutdownHook.Shutdown.class);
        shutdownHook.add(ShutdownHook.STAGE_0, shutdown1);
        shutdownHook.add(ShutdownHook.STAGE_10, shutdown2);

        shutdownHook.run();
        verify(shutdown2).execute(anyLong());
    }
}
