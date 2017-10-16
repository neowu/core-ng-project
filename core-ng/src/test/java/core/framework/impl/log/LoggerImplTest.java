package core.framework.impl.log;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class LoggerImplTest {
    @Test
    void abbreviateLoggerName() {
        assertEquals("Bean", LoggerImpl.abbreviateLoggerName("Bean"));
        assertEquals("c.Bean", LoggerImpl.abbreviateLoggerName("core.Bean"));
        assertEquals("c.f.Bean", LoggerImpl.abbreviateLoggerName("core.framework.Bean"));
        assertEquals("c.f.a.Bean", LoggerImpl.abbreviateLoggerName("core.framework.api.Bean"));
        assertEquals("c.f.a.module.Bean", LoggerImpl.abbreviateLoggerName("core.framework.api.module.Bean"));
    }
}
