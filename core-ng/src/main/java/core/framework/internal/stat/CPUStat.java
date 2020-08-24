package core.framework.internal.stat;

import com.sun.management.OperatingSystemMXBean;

/**
 * @author neo
 */
class CPUStat {
    private final OperatingSystemMXBean os;
    private final int availableProcessors;

    private long previousCPUTime;
    private long previousTime;

    CPUStat(OperatingSystemMXBean os) {
        this.os = os;
        this.availableProcessors = os.getAvailableProcessors();

        previousTime = System.nanoTime();
        previousCPUTime = os.getProcessCpuTime();
    }

    // due to os.getProcessCpuLoad() returns host level info, not for container, so here uses process time to calculate container level java cpu usage
    // be aware there is limitation，the availableProcessors is always round up to nearest integer
    // e.g. if limit.cpu=500m, max cpu usage is 50%
    // for limit.cpu=1000m, max cpu usage is 100%
    // for limit.cpu=1500m, max cpu usage is 150/200=75%
    double usage() {
        long currentTime = System.nanoTime();
        long currentCPUTime = os.getProcessCpuTime();

        double usage = usage(currentTime, previousTime, currentCPUTime, previousCPUTime, availableProcessors);

        previousCPUTime = currentCPUTime;
        previousTime = currentTime;

        return usage;
    }

    double usage(long currentTime, long previousTime, long currentCPUTime, long previousCPUTime, int availableProcessors) {
        return (double) (currentCPUTime - previousCPUTime) / (currentTime - previousTime) / availableProcessors;
    }
}
