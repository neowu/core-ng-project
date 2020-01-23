package core.framework.internal.log;

import core.framework.internal.log.appender.LogAppender;
import core.framework.internal.stat.StatCollector;
import core.framework.internal.stat.Stats;
import core.framework.log.message.StatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        task = new CollectStatTask(appender, new StatCollector());
    }

    @Test
    void message() {
        Stats stats = new Stats();
        stats.put("sys_load_avg", 1d);
        StatMessage message = task.message(stats);
        assertThat(message.id).isNotNull();
        assertThat(message.stats).containsOnly(entry("sys_load_avg", 1d));
    }

    @Test
    void appendWithError() {
        doThrow(new Error()).when(appender).append(any(StatMessage.class));
        task.run();
    }
}
