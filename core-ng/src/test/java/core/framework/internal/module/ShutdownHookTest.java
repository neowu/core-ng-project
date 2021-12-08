package core.framework.internal.module;

import core.framework.internal.log.ActionLog;
import core.framework.internal.log.LogManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
        shutdownHook = new ShutdownHook(new LogManager());
    }

    @Test
    void run() throws Exception {
        var shutdown1 = mock(ShutdownHook.Shutdown.class);
        doThrow(new Error("failed to shutdown")).when(shutdown1).execute(anyLong());
        var shutdown2 = mock(ShutdownHook.Shutdown.class);
        shutdownHook.add(ShutdownHook.STAGE_0, shutdown1);
        shutdownHook.add(ShutdownHook.STAGE_6, shutdown2);

        shutdownHook.run();
        verify(shutdown2).execute(anyLong());
    }

    @Test
    void logContext() {
        var actionLog = new ActionLog(null, null);
        shutdownHook.logContext(actionLog);

        assertThat(actionLog.action).isEqualTo("app:stop");
        assertThat(actionLog.context).containsKey("start_time");
        assertThat(actionLog.stats).containsKey("uptime");
    }

    @Test
    void shutdownTimeoutInNano() {
        assertThat(shutdownHook.shutdownTimeoutInNano(Map.of())).isEqualTo(Duration.ofSeconds(25).toNanos());    // default is 25s

        assertThat(shutdownHook.shutdownTimeoutInNano(Map.of("SHUTDOWN_TIMEOUT_IN_SEC", "60")))
            .isEqualTo(Duration.ofSeconds(60).toNanos());

        assertThatThrownBy(() -> shutdownHook.shutdownTimeoutInNano(Map.of("SHUTDOWN_TIMEOUT_IN_SEC", "-1")))
            .isInstanceOf(Error.class)
            .hasMessageContaining("greater than 0");
    }

    @Test
    void shutdownDelayInSec() {
        assertThat(shutdownHook.shutdownDelayInSec(Map.of())).isEqualTo(-1);    // default to no delay

        assertThat(shutdownHook.shutdownDelayInSec(Map.of("SHUTDOWN_DELAY_IN_SEC", "10")))
                .isEqualTo(10);

        assertThatThrownBy(() -> shutdownHook.shutdownDelayInSec(Map.of("SHUTDOWN_DELAY_IN_SEC", "-1")))
                .isInstanceOf(Error.class)
                .hasMessageContaining("greater than 0");
    }

    @Test
    void shutdown() throws Exception {
        var shutdown = mock(ShutdownHook.Shutdown.class);
        shutdownHook.add(ShutdownHook.STAGE_6, shutdown);
        shutdownHook.shutdown(System.currentTimeMillis(), ShutdownHook.STAGE_6, ShutdownHook.STAGE_8);

        verify(shutdown).execute(1000); // give resource as least 1s to shutdown
    }
}
