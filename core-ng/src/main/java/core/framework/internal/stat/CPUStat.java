package core.framework.internal.stat;

import com.sun.management.OperatingSystemMXBean;

/**
 * @author neo
 */
class CPUStat {
    private final OperatingSystemMXBean os;
    private final int availableProcessors;

    private long previousCPUTime = -1;
    private long previousTime = -1;

    CPUStat(OperatingSystemMXBean os) {
        this.os = os;
        this.availableProcessors = os.getAvailableProcessors();
    }

    // due to os.getProcessCpuLoad() returns host level info, not for container, so here uses process time to calculate container level java cpu usage
    // be aware there is limitation，the availableProcessors is always round up to the nearest integer
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
        if (previousCPUTime == -1) return 0;    // ignore first data point, to collect cpu usage after startup
        return (double) (currentCPUTime - previousCPUTime) / (currentTime - previousTime) / availableProcessors;
    }
}
