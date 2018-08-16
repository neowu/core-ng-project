package core.log.job;

import core.framework.impl.log.message.StatMessage;
import core.framework.inject.Inject;
import core.log.IntegrationTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author neo
 */
class CollectStatTaskTest extends IntegrationTest {
    @Inject
    CollectStatTask task;

    @Test
    void message() {
        StatMessage message = task.message();

        assertEquals("log-processor", message.app);
        assertNotNull(message.stats.get("sys_load_avg"));
    }
}
