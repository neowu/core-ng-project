package core.framework.module;

import core.framework.internal.log.LogManager;
import core.framework.internal.module.ModuleContext;
import core.framework.log.LogAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

/**
 * @author neo
 */
class LogConfigTest {
    private LogConfig config;

    @BeforeEach
    void createLogConfig() {
        config = new LogConfig();
        config.initialize(new ModuleContext(new LogManager()), null);
    }

    @Test
    void appender() {
        LogAppender appender = mock(LogAppender.class);
        config.appender(appender);

        assertThatThrownBy(() -> config.appender(appender))
            .isInstanceOf(Error.class)
            .hasMessageContaining("log appender is already set");
    }

    @Test
    void appendToKafka() {
        config.appendToConsole();

        assertThatThrownBy(() -> config.appendToKafka("kafka:9092"))
            .isInstanceOf(Error.class)
            .hasMessageContaining("log appender is already set");
    }
}
