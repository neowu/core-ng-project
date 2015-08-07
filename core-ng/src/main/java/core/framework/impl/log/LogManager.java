package core.framework.impl.log;

import core.framework.api.util.Charsets;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

/**
 * @author neo
 */
public class LogManager {
    private final ThreadLocal<ActionLog> logs = new ThreadLocal<>();
    private Writer actionLogWriter = new BufferedWriter(new OutputStreamWriter(System.out, Charsets.UTF_8));
    private Path traceLogPath;

    public void start() {
        logs.set(new ActionLog(traceLogPath));
    }

    public void end() {
        ActionLog log = logs.get();
        log.end(actionLogWriter);
        logs.remove();
    }

    public void process(LogEvent event) {
        ActionLog log = logs.get();
        if (log != null) log.process(event);
    }

    public void actionLogPath(Path actionLogPath) {
        Path path = actionLogPath.toAbsolutePath();
        try {
            Files.createDirectories(path.getParent());
            if (Files.notExists(path)) Files.createFile(path);
            if (!Files.isWritable(path)) throw new IOException("action log file is not writable, path=" + path);
            actionLogWriter = Files.newBufferedWriter(path, Charsets.UTF_8, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void traceLogPath(Path traceLogPath) {
        try {
            Files.createDirectories(traceLogPath);
            this.traceLogPath = traceLogPath;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void close() {
        try {
            actionLogWriter.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Optional<String> get(String key) {
        ActionLog log = logs.get();
        if (log == null) return Optional.empty();
        return Optional.ofNullable(log.context.get(key));
    }

    public void put(String key, Object value) {
        ActionLog log = logs.get();
        if (log == null) return;
        log.context.put(key, String.valueOf(value));
    }

    public void track(String action, long elapsedTime) {
        ActionLog log = logs.get();
        if (log == null) return;

        TimeTracking tracking = log.tracking.computeIfAbsent(action, key -> new TimeTracking());
        tracking.count++;
        tracking.totalElapsedTime += elapsedTime;
    }
}
