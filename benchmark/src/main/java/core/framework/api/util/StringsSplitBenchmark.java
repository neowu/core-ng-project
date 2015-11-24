package core.framework.api.util;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author neo
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 3)
@Measurement(iterations = 10)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class StringsSplitBenchmark {
    private final List<String> texts = Lists.newArrayList();

    @Setup
    public void setup() {
        texts.add("path1/path2/path3");
        texts.add("path1/path2/path3/");
        texts.add("/path1/path2/path3");
        texts.add("/long-0123456789-path1/long-0123456789-path2/long-0123456789-path3");
    }

    @Benchmark
    public void current() {
        for (String text : texts) {
            Strings.split(text, '/');
        }
    }

    @Benchmark
    public void jdk() {
        for (String text : texts) {
            text.split("/");
        }
    }
}
