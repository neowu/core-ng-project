package core.framework.impl.log.stat;

import core.framework.impl.log.KafkaAppender;

import java.util.Map;

/**
 * @author neo
 */
public final class CollectStatTask implements Runnable {
    private final KafkaAppender kafkaAppender;
    private final Stat stat;

    public CollectStatTask(KafkaAppender kafkaAppender, Stat stat) {
        this.kafkaAppender = kafkaAppender;
        this.stat = stat;
    }

    @Override
    public void run() {
        Map<String, Double> stats = stat.collect();
        kafkaAppender.forwardStats(stats);
    }
}
