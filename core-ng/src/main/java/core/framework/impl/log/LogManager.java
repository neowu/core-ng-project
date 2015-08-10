package core.framework.impl.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author neo
 */
public class LogManager {
    private final ThreadLocal<ActionLogger> loggers = new ThreadLocal<>();
    public final LogWriter logWriter = new LogWriter();
    public final String appName;

    public LogManager() {
        this.appName = System.getProperty("core.appName");
    }

    public void start() {
        loggers.set(new ActionLogger(logWriter));
    }

    public void end() {
        ActionLogger logger = loggers.get();
        logger.end();
        loggers.remove();
    }

    public void process(LogEvent event) {
        ActionLogger logger = loggers.get();
        if (logger != null) logger.process(event);
    }

    public ActionLog currentActionLog() {
        ActionLogger logger = loggers.get();
        if (logger == null) return null;
        return logger.log;
    }

    public void shutdown() {
        Logger logger = LoggerFactory.getLogger(LogManager.class.getName());
        logger.info("showdown log manager");
        logWriter.close();
    }
}
