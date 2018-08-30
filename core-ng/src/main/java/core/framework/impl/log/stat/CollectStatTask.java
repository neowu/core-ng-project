package core.framework.impl.log.stat;

import core.framework.impl.log.KafkaAppender;

import java.util.Map;

/**
 * @author neo
 */
public final class CollectStatTask implements Runnable {
    private final KafkaAppender appender;
    private final Stat stat;

    public CollectStatTask(KafkaAppender appender, Stat stat) {
        this.appender = appender;
        this.stat = stat;
    }

    @Override
    public void run() {
        Map<String, Double> stats = stat.collect();
        appender.forward(stats);
    }
}
