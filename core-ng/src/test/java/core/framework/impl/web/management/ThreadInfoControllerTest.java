package core.framework.impl.web.management;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

/**
 * @author neo
 */
class ThreadInfoControllerTest {
    private ThreadInfoController controller;

    @BeforeEach
    void createThreadInfoController() {
        controller = new ThreadInfoController();
    }

    @Test
    void threadDump() {
        String threadDump = controller.threadDumpText();
        assertThat(threadDump, containsString("core.framework.impl.web.management.ThreadInfoController.threadDumpText"));
    }
}
