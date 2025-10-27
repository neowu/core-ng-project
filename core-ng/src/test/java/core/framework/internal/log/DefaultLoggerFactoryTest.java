package core.framework.internal.log;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultLoggerFactoryTest {
    private DefaultLoggerFactory factory;

    @BeforeEach
    void createDefaultLoggerFactory() {
        factory = new DefaultLoggerFactory();
    }

    @Test
    void logLevel() {
        Logger logger = factory.getLogger("org.apache.kafka.common.config.AbstractConfig");

        assertThat(logger).isInstanceOf(LoggerImpl.class);
        assertThat(((LoggerImpl) logger).infoLevel).isEqualTo(LogLevel.WARN);
        assertThat(((LoggerImpl) logger).traceLevel).isEqualTo(LogLevel.DEBUG);
    }
}
