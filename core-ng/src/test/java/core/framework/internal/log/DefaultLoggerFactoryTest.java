package core.framework.internal.log;

import core.framework.log.LogLevel;
import core.framework.log.LogLevels;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultLoggerFactoryTest {
    private DefaultLoggerFactory factory;

    @BeforeEach
    void createDefaultLoggerFactory() {
        factory = new DefaultLoggerFactory(new LogLevels.Entry[]{new LogLevels.Entry("com.test.", LogLevel.WARN)},
            new LogLevels.Entry[]{new LogLevels.Entry("com.test.", LogLevel.INFO)});
    }

    @Test
    void logLevel() {
        Logger logger = factory.getLogger("com.test.Test");

        assertThat(logger).isInstanceOf(LoggerImpl.class);
        assertThat(((LoggerImpl) logger).infoLevel).isEqualTo(LogLevel.WARN);
        assertThat(((LoggerImpl) logger).traceLevel).isEqualTo(LogLevel.INFO);
    }
}
