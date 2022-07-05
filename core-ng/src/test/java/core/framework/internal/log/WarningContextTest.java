package core.framework.internal.log;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author neo
 */
class WarningContextTest {
    private WarningContext context;

    @BeforeEach
    void createWarningContext() {
        context = new WarningContext();
    }

    @Test
    void checkMaxProcessTime() {
        context.maxProcessTimeInNano = -1;
        context.checkMaxProcessTime(100);

        context.maxProcessTimeInNano = 100;
        context.checkMaxProcessTime(81);
    }
}
