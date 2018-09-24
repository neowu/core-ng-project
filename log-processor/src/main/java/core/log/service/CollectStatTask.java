package core.log.service;

import core.framework.impl.log.MessageFactory;
import core.framework.impl.log.message.StatMessage;
import core.framework.impl.log.stat.Stat;
import core.framework.inject.Inject;

import java.util.Map;

/**
 * @author neo
 */
public class CollectStatTask implements Runnable {
    private final Stat stat;
    @Inject
    StatService statService;

    public CollectStatTask(Stat stat) {
        this.stat = stat;
    }

    @Override
    public void run() {
        StatMessage message = message();
        statService.index(message);
    }

    StatMessage message() {
        Map<String, Double> stats = stat.collect();
        return MessageFactory.stat(stats);
    }
}
