package core.framework.api.util;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.List;

/**
 * @author neo
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 3)
@Measurement(iterations = 10)
public class URIEncodingBenchmark {
    private final List<String> pathSegments = Lists.newArrayList();

    @Setup
    public void setup() {
        pathSegments.add("path1");
        pathSegments.add("path2");
        pathSegments.add("path3");
        pathSegments.add("value1 value2");
        pathSegments.add("value1+value2");
        pathSegments.add("value1/value2");
        pathSegments.add("/value1");
        pathSegments.add("utf-8-✓");
        pathSegments.add("value1?value2");
    }

    @Benchmark
    public void current() {
        for (String pathSegment : pathSegments) {
            Encodings.uriComponent(pathSegment);
        }
    }
}
