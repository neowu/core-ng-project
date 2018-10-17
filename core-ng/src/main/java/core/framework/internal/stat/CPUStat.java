package core.framework.internal.stat;

import core.framework.util.Maps;

import java.lang.management.ThreadMXBean;
import java.util.Map;

/**
 * @author neo
 */
class CPUStat {
    private final ThreadMXBean thread;
    private final int availableProcessors;

    private Map<Long, Long> previousThreadCPUTimes; // generally only one thread collects stat, so no concurrency issue
    private long previousTime;

    CPUStat(ThreadMXBean thread, int availableProcessors) {
        this.thread = thread;
        this.availableProcessors = availableProcessors;

        previousTime = System.nanoTime();
        previousThreadCPUTimes = threadCPUTimes();
    }

    double usage() {
        Map<Long, Long> currentThreadCPUTimes = threadCPUTimes();
        long currentTime = System.nanoTime();

        double usage = usage(currentThreadCPUTimes, currentTime, previousThreadCPUTimes, previousTime);

        previousThreadCPUTimes = currentThreadCPUTimes;
        previousTime = currentTime;

        return usage;
    }

    double usage(Map<Long, Long> currentThreadCPUTimes, long currentTime, Map<Long, Long> previousThreadCPUTimes, long previousTime) {
        long usedCPUTime = 0;
        for (Map.Entry<Long, Long> entry : currentThreadCPUTimes.entrySet()) {
            Long threadId = entry.getKey();
            usedCPUTime += entry.getValue() - previousThreadCPUTimes.getOrDefault(threadId, 0L);
        }

        return (double) usedCPUTime / ((currentTime - previousTime) * availableProcessors);
    }

    private Map<Long, Long> threadCPUTimes() {
        long[] threadIds = thread.getAllThreadIds();
        Map<Long, Long> threadCPUTimes = Maps.newHashMapWithExpectedSize(threadIds.length);
        for (long threadId : threadIds) {
            threadCPUTimes.put(threadId, thread.getThreadCpuTime(threadId));
        }
        return threadCPUTimes;
    }
}
