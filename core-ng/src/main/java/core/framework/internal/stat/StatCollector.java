package core.framework.internal.stat;

import com.sun.management.OperatingSystemMXBean;
import core.framework.internal.async.VirtualThread;
import core.framework.util.Files;
import core.framework.util.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @author neo
 */
public class StatCollector {
    public final List<Metrics> metrics = Lists.newArrayList();

    private final Logger logger = LoggerFactory.getLogger(StatCollector.class);
    private final OperatingSystemMXBean os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    private final ThreadMXBean thread = ManagementFactory.getThreadMXBean();
    private final MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
    private final List<GCStat> gcStats = new ArrayList<>(2);
    private final boolean supportMemoryTracking;
    private final Path procPath = Path.of("/proc/self/statm");

    public double highCPUUsageThreshold = 0.8;
    public double highHeapUsageThreshold = 0.8;
    public double highMemUsageThreshold = 0.8;  // the java process RSS usage

    public StatCollector() {
        List<GarbageCollectorMXBean> beans = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean bean : beans) {
            GCStat stat = GCStat.of(bean);
            if (stat != null) gcStats.add(stat);
        }
        supportMemoryTracking = java.nio.file.Files.exists(procPath);
    }

    public void collectJVMUsage(Stats stats) {
        collectCPUUsage(stats);
        stats.put("thread_count", thread.getThreadCount());
        stats.put("virtual_thread_count", VirtualThread.COUNT.max());
        collectHeapUsage(stats);

        for (GCStat gcStat : gcStats) {
            double count = gcStat.count();
            double elapsed = gcStat.elapsed();
            stats.put("jvm_gc_" + gcStat.name + "_count", count);
            stats.put("jvm_gc_" + gcStat.name + "_elapsed", elapsed);
        }
    }

    // collect VmRSS / cgroup ram limit
    // refer to https://man7.org/linux/man-pages/man5/proc.5.html, /proc/[pid]/statm section
    // e.g. 913415 52225 7215 1 0 66363 0
    // the second number is VmRSS in pages (4k)
    public void collectMemoryUsage(Stats stats) {
        if (!supportMemoryTracking) return;

        var content = new String(Files.bytes(procPath), StandardCharsets.US_ASCII);
        double vmRSS = parseVmRSS(content);
        stats.put("vm_rss", vmRSS);
        double maxMemory = os.getTotalMemorySize();
        stats.put("mem_max", maxMemory);
        boolean highUsage = stats.checkHighUsage(vmRSS / maxMemory, highMemUsageThreshold, "mem");
        if (highUsage) {
            stats.info("proc_status", new String(Files.bytes(Path.of("/proc/self/status")), StandardCharsets.US_ASCII));
            stats.info("native_memory", Diagnostic.nativeMemory());
        }
    }

    long parseVmRSS(String content) {
        int index1 = content.indexOf(' ');
        int index2 = content.indexOf(' ', index1 + 1);
        return Long.parseLong(content.substring(index1 + 1, index2)) * 4096;
    }

    public void collectMetrics(Stats stats) {
        for (Metrics metrics : metrics) {
            try {
                metrics.collect(stats);
            } catch (Throwable e) {
                logger.warn("failed to collect metrics, metrics={}, error={}", metrics.getClass().getCanonicalName(), e.getMessage(), e);
            }
        }
    }

    private void collectHeapUsage(Stats stats) {
        MemoryUsage heapUsage = memory.getHeapMemoryUsage();
        double usedHeap = heapUsage.getUsed();
        double maxHeap = heapUsage.getMax();
        stats.put("jvm_heap_used", usedHeap);
        stats.put("jvm_heap_max", maxHeap);
        stats.checkHighUsage(usedHeap / maxHeap, highHeapUsageThreshold, "heap");

        MemoryUsage nonHeapUsage = memory.getNonHeapMemoryUsage();
        stats.put("jvm_non_heap_used", nonHeapUsage.getUsed());
    }

    private void collectCPUUsage(Stats stats) {
        stats.put("sys_load_avg", os.getSystemLoadAverage());   // until java 15, OperatingSystemMXBean returns host level load and cpu usage, not container level

        // since java 17, JVM is aware of container
        // use "java -XshowSettings:system -version" to show container info
        // refer to com.sun.management.internal.OperatingSystemImpl.ContainerCpuTicks.getContainerCpuLoad
        double usage = os.getProcessCpuLoad();
        stats.put("cpu_usage", usage);
        boolean highUsage = stats.checkHighUsage(usage, highCPUUsageThreshold, "cpu");
        if (highUsage) {
            stats.info("thread_dump", Diagnostic.thread());
            stats.info("virtual_thread_dump", Diagnostic.virtualThread());
        }
    }
}
