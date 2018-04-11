package core.framework.impl.web.management;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(threadDump).contains("core.framework.impl.web.management.ThreadInfoController.threadDumpText");
    }
}
