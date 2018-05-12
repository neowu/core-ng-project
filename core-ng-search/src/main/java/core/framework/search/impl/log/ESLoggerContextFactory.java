package core.framework.search.impl.log;

import org.apache.logging.log4j.spi.LoggerContext;
import org.apache.logging.log4j.spi.LoggerContextFactory;

import java.net.URI;

// refer to org.apache.logging.log4j:log4j-to-slf4j
public class ESLoggerContextFactory implements LoggerContextFactory {
    private static final LoggerContext CONTEXT = new ESLoggerContext();

    @Override
    public LoggerContext getContext(String fqcn, ClassLoader loader, Object externalContext, boolean currentContext) {
        return CONTEXT;
    }

    @Override
    public LoggerContext getContext(String fqcn, ClassLoader loader, Object externalContext, boolean currentContext, URI configLocation, String name) {
        return CONTEXT;
    }

    @Override
    public void removeContext(LoggerContext context) {
    }
}
