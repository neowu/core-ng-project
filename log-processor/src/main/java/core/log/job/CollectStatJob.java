package core.log.job;

import core.framework.impl.log.message.StatMessage;
import core.framework.impl.log.stat.Stat;
import core.framework.inject.Inject;
import core.framework.scheduler.Job;
import core.framework.util.Network;
import core.log.service.StatService;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * @author neo
 */
public class CollectStatJob implements Job {
    @Inject
    StatService statService;
    @Inject
    Stat stat;

    @Override
    public void execute() {
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
