package core.framework.impl.log;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author neo
 */
public class LoggerImplTest {
    @Test
    public void abbreviateLoggerName() {
        Assert.assertEquals("Bean", LoggerImpl.abbreviateLoggerName("Bean"));
        Assert.assertEquals("c.Bean", LoggerImpl.abbreviateLoggerName("core.Bean"));
        Assert.assertEquals("c.f.Bean", LoggerImpl.abbreviateLoggerName("core.framework.Bean"));
        Assert.assertEquals("c.f.a.Bean", LoggerImpl.abbreviateLoggerName("core.framework.api.Bean"));
        Assert.assertEquals("c.f.a.module.Bean", LoggerImpl.abbreviateLoggerName("core.framework.api.module.Bean"));
    }
}