package core.framework.api.util;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * @author neo
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 3)
@Measurement(iterations = 10)
public class StopWatchBenchmark {
    private ThreadMXBean bean;

    @Setup
    public void setup() {
        bean = ManagementFactory.getThreadMXBean();
    }

    @Benchmark
    public void milliTime() {
        System.currentTimeMillis();
    }

    @Benchmark
    public void nanoTime() {
        System.nanoTime();
    }

    @Benchmark
    public void cpuTime() {
        bean.getCurrentThreadCpuTime();
    }
}
