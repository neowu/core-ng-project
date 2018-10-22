package core.framework.internal.log;

import core.framework.internal.log.appender.LogAppender;
import core.framework.internal.log.message.StatMessage;
import core.framework.internal.stat.Stat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

/**
 * @author neo
 */
class CollectStatTaskTest {
    private CollectStatTask task;
    private LogAppender appender;

    @BeforeEach
    void createCollectStatTask() {
        appender = mock(LogAppender.class);
        task = new CollectStatTask(appender, new Stat());
    }

    @Test
    void message() {
        StatMessage message = task.message(Map.of("sys_load_avg", 1d));
        assertThat(message.id).isNotNull();
        assertThat(message.stats).containsOnly(entry("sys_load_avg", 1d));
    }

    @Test
    void appendWithError() {
        doThrow(new Error()).when(appender).append(any(StatMessage.class));
        task.run();
    }
}
