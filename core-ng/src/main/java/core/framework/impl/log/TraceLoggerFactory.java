package core.framework.impl.log;

import core.framework.api.util.Files;

import java.nio.file.Path;

/**
 * @author neo
 */
public final class TraceLoggerFactory {
    public static TraceLoggerFactory console() {
        return new TraceLoggerFactory(null);
    }

    public static TraceLoggerFactory file(Path traceLogPath) {
        Files.createDir(traceLogPath);
        return new TraceLoggerFactory(traceLogPath);
    }

    private final Path traceLogPath;

    private TraceLoggerFactory(Path traceLogPath) {
        this.traceLogPath = traceLogPath;
    }

    TraceLogger create(ActionLog actionLog, LogForwarder logForwarder) {
        return new TraceLogger(traceLogPath, actionLog, logForwarder);
    }
}
