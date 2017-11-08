package core.log.job;

import core.framework.inject.Inject;
import core.framework.log.message.StatMessage;
import core.framework.scheduler.Job;
import core.framework.util.Maps;
import core.framework.util.Network;
import core.log.service.MessageProcessor;
import core.log.service.StatService;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * @author neo
 */
public class CollectStatJob implements Job {
    private final OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
    private final ThreadMXBean thread = ManagementFactory.getThreadMXBean();
    private final MemoryMXBean memory = ManagementFactory.getMemoryMXBean();

    @Inject
    MessageProcessor messageProcessor;
    @Inject
    StatService statService;

    @Override
    public void execute() throws Exception {
        StatMessage message = message();
        statService.index(message);
    }

    StatMessage message() {
        Map<String, Double> stats = Maps.newLinkedHashMap();
        stats.put("sys_load_avg", os.getSystemLoadAverage());
        stats.put("thread_count", (double) thread.getThreadCount());
        MemoryUsage usage = memory.getHeapMemoryUsage();
        stats.put("jvm_heap_used", (double) usage.getUsed());
        stats.put("jvm_heap_max", (double) usage.getMax());

        messageProcessor.metrics.collect(stats);

        StatMessage message = new StatMessage();
        message.id = UUID.randomUUID().toString();
        message.date = Instant.now();
        message.app = "log-processor";
        message.serverIP = Network.localHostAddress();
        message.stats = stats;
        return message;
    }
}
