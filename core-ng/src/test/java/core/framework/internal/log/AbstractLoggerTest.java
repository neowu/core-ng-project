package core.framework.internal.log;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.verify;

/**
 * @author neo
 */
class AbstractLoggerTest {
    @Test
    void logWithEmptyArguments() {
        AbstractLogger logger = Mockito.mock(AbstractLogger.class, Mockito.CALLS_REAL_METHODS);
        Object[] arguments = new Object[0];
        logger.debug("test", arguments);
        verify(logger).log(null, LogLevel.DEBUG, "test", new Object[0], null);
    }
}
