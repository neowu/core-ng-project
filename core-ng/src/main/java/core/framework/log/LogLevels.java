package core.framework.log;

import core.framework.internal.log.DefaultLoggerFactory;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class LogLevels {
    @SuppressFBWarnings("PMB_POSSIBLE_MEMORY_BLOAT")
    @Nullable
    private static List<Entry> infoLevels = new ArrayList<>();
    @SuppressFBWarnings("PMB_POSSIBLE_MEMORY_BLOAT")
    @Nullable
    private static List<Entry> traceLevels = new ArrayList<>();

    static {
        infoLevels.add(new Entry("org.apache.kafka", LogLevel.WARN));                   // kafka log info for every producer/consumer, to reduce verbosity
        infoLevels.add(new Entry("org.elasticsearch.nativeaccess", LogLevel.ERROR));    // refer to org.elasticsearch.nativeaccess.NativeAccessHolder, to emmit warning under integration-test env

        traceLevels.add(new Entry("org.elasticsearch", LogLevel.INFO));
        traceLevels.add(new Entry("org.mongodb", LogLevel.INFO));
        traceLevels.add(new Entry("org.xnio", LogLevel.INFO));
    }

    // action log trace collects logs where level >= traceLevel, logs where level >= infoLevel will be printed out to console always
    public static void add(String prefix, LogLevel traceLevel, LogLevel infoLevel) {
        if (traceLevels == null || infoLevels == null) throw new Error("log levels must be configured before slf4j initialization");
        if (traceLevel.value > infoLevel.value)
            throw new Error("log with info level less than trace level may not be printed, prefix=" + prefix);  // refer to core.framework.internal.log.LoggerImpl.log()
        traceLevels.add(new Entry(prefix, traceLevel));
        infoLevels.add(new Entry(prefix, infoLevel));
    }

    public static DefaultLoggerFactory createLoggerFactory() {
        return new DefaultLoggerFactory(infoLevels.toArray(new Entry[0]), traceLevels.toArray(new Entry[0]));
    }

    public static void cleanup() {
        infoLevels = null;
        traceLevels = null;
    }

    public record Entry(String prefix, LogLevel level) {
    }
}
