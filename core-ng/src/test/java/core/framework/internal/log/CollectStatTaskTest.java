package core.framework.internal.log;

import core.framework.internal.stat.StatCollector;
import core.framework.internal.stat.Stats;
import core.framework.log.LogAppender;
import core.framework.log.message.StatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

/**
 * @author neo
 */
@ExtendWith(MockitoExtension.class)
class CollectStatTaskTest {
    @Mock
    LogAppender appender;
    private CollectStatTask task;

    @BeforeEach
    void createCollectStatTask() {
        task = new CollectStatTask(appender, new StatCollector());
    }

    @Test
    void message() {
        var stats = new Stats();
        stats.put("sys_load_avg", 1d);
        StatMessage message = task.message(stats);
        assertThat(message.id).isNotNull();
        assertThat(message.stats).containsOnly(entry("sys_load_avg", 1d));
    }

    @Test
    void run() {
        task.run();
        verify(appender).append(any(StatMessage.class));
    }
}
