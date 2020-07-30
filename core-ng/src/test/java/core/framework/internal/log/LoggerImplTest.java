package core.framework.internal.log;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class LoggerImplTest {
    @Test
    void abbreviateLoggerName() {
        assertThat(LoggerImpl.abbreviateLoggerName("Bean")).isEqualTo("Bean");
        assertThat(LoggerImpl.abbreviateLoggerName("core.Bean")).isEqualTo("c.Bean");
        assertThat(LoggerImpl.abbreviateLoggerName("core.framework.Bean")).isEqualTo("c.f.Bean");
        assertThat(LoggerImpl.abbreviateLoggerName("core.framework.api.Bean")).isEqualTo("c.f.a.Bean");
        assertThat(LoggerImpl.abbreviateLoggerName("core.framework.api.module.Bean")).isEqualTo("c.f.a.module.Bean");
        assertThat(LoggerImpl.abbreviateLoggerName("core.framework.api.module.service.Bean")).isEqualTo("c.f.a.m.service.Bean");
        assertThat(LoggerImpl.abbreviateLoggerName("core.framework.api.module.service.impl.Bean")).isEqualTo("c.f.a.m.s.impl.Bean");
    }

    @Test
    void getLogger() {
        Logger logger1 = LoggerFactory.getLogger(LoggerImplTest.class);
        Logger logger2 = LoggerFactory.getLogger(LoggerImplTest.class);

        assertThat(logger1).isSameAs(logger2);
    }
}
