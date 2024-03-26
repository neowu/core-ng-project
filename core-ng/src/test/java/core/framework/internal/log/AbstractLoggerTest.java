package core.framework.internal.log;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

/**
 * @author neo
 */
@ExtendWith(MockitoExtension.class)
class AbstractLoggerTest {
    @Mock(answer = Answers.CALLS_REAL_METHODS)
    AbstractLogger logger;

    @SuppressWarnings("PMD.InvalidLogMessageFormat")    // test the target method intentionally
    @Test
    void logWithEmptyArguments() {
        Object[] arguments = new Object[0];
        logger.debug("test", arguments);
        verify(logger).log(null, LogLevel.DEBUG, "test", new Object[0], null);
    }
}
