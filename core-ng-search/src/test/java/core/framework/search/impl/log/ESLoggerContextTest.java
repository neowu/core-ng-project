package core.framework.search.impl.log;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.spi.ExtendedLogger;
import org.apache.logging.log4j.spi.LoggerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class ESLoggerContextTest {
    private LoggerContext context;

    @BeforeEach
    void createESLoggerContext() {
        context = new ESLoggerContextFactory().getContext(null, null, null, true);
    }

    @Test
    void getLogger() {
        ExtendedLogger logger1 = context.getLogger("test");
        ExtendedLogger logger2 = context.getLogger("test");
        assertThat(logger1).isSameAs(logger2);

        assertThat(logger1.isEnabled(Level.DEBUG)).isFalse();
        assertThat(logger1.isEnabled(Level.INFO)).isTrue();
        assertThat(logger1.isEnabled(Level.WARN)).isTrue();

        logger1.log(Level.INFO, "test");
    }
}
