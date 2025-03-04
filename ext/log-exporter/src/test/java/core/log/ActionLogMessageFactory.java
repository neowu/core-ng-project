package core.log;

import core.framework.log.message.ActionLogMessage;
import core.framework.log.message.PerformanceStatMessage;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ActionLogMessageFactory {
    public static ActionLogMessage create() {
        var message = new ActionLogMessage();
        message.date = Instant.parse("2022-11-07T00:00:00Z");
        message.id = "id";
        message.app = "app";
        message.action = "action";
        message.result = "OK";
        message.host = "host";
        message.elapsed = 1000L;
        List<String> keys = new ArrayList<>();
        keys.add(null);
        message.context = Map.of("customer_id", List.of("customer_id1", "customer_id2"), "key", keys);
        message.performanceStats = Map.of("kafka", perfStats(1, 1000L, 10, 5),
            "http", perfStats(2, 2000L, null, null));
        return message;
    }

    private static PerformanceStatMessage perfStats(int count, long totalElapsed, Integer readEntries, Integer writeEntries) {
        final PerformanceStatMessage stats = new PerformanceStatMessage();
        stats.count = count;
        stats.totalElapsed = totalElapsed;
        stats.readEntries = readEntries;
        stats.writeEntries = writeEntries;
        return stats;
    }
}
