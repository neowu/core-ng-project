package core.framework.impl.log;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author neo
 */
public class LoggerImplTest {
    LoggerImpl logger;

    @Before
    public void createDefaultLogger() {
        logger = new LoggerImpl("", null, null, null);
    }

    @Test
    public void abbreviateLoggerName() {
        Assert.assertEquals("Bean", logger.abbreviateLoggerName("Bean"));
        Assert.assertEquals("c.Bean", logger.abbreviateLoggerName("core.Bean"));
        Assert.assertEquals("c.f.Bean", logger.abbreviateLoggerName("core.framework.Bean"));
        Assert.assertEquals("c.f.a.Bean", logger.abbreviateLoggerName("core.framework.api.Bean"));
        Assert.assertEquals("c.f.a.module.Bean", logger.abbreviateLoggerName("core.framework.api.module.Bean"));
    }
}