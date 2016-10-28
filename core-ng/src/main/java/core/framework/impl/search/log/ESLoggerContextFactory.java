package core.framework.impl.search.log;

import org.apache.logging.log4j.spi.LoggerContext;
import org.apache.logging.log4j.spi.LoggerContextFactory;

import java.net.URI;

// refer to org.apache.logging.log4j:log4j-to-slf4j
public class ESLoggerContextFactory implements LoggerContextFactory {
    private static LoggerContext context = new ESLoggerContext();

    @Override
    public LoggerContext getContext(String fqcn, ClassLoader loader, Object externalContext, boolean currentContext) {
        return context;
    }

    @Override
    public LoggerContext getContext(String fqcn, ClassLoader loader, Object externalContext, boolean currentContext, URI configLocation, String name) {
        return context;
    }

    @Override
    public void removeContext(LoggerContext context) {
    }
}
