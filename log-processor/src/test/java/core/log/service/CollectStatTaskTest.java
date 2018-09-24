package core.log.service;

import core.framework.impl.log.LogManager;
import core.framework.impl.log.message.StatMessage;
import core.framework.inject.Inject;
import core.log.IntegrationTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class CollectStatTaskTest extends IntegrationTest {
    @Inject
    CollectStatTask task;

    @Test
    void message() {
        StatMessage message = task.message();

        assertThat(message.app).isEqualTo(LogManager.APP_NAME);
        assertThat(message.stats.get("sys_load_avg")).isNotNull();
    }
}
