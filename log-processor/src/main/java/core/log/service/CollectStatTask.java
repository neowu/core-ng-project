package core.log.service;

import core.framework.impl.log.message.StatMessage;
import core.framework.impl.log.stat.Stat;
import core.framework.inject.Inject;
import core.framework.util.Network;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

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

        StatMessage message = new StatMessage();
        message.id = UUID.randomUUID().toString();
        message.date = Instant.now();
        message.app = "log-processor";
        message.serverIP = Network.localHostAddress();
        message.stats = stats;
        return message;
    }
}
