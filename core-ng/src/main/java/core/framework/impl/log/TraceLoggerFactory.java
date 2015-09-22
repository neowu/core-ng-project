package core.framework.impl.log;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author neo
 */
public final class TraceLoggerFactory {
    public static TraceLoggerFactory console() {
        return new TraceLoggerFactory(null);
    }

    public static TraceLoggerFactory file(Path traceLogPath) {
        try {
            Files.createDirectories(traceLogPath);
            return new TraceLoggerFactory(traceLogPath);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private final Path traceLogPath;

    private TraceLoggerFactory(Path traceLogPath) {
        this.traceLogPath = traceLogPath;
    }

    TraceLogger create(ActionLog actionLog, LogForwarder logForwarder) {
        return new TraceLogger(traceLogPath, actionLog, logForwarder);
    }
}
