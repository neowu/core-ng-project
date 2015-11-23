package core.framework.api.util;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author neo
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class URIBuilderEncodePathSegmentBenchmark {
    private final List<String> pathSegments = Lists.newArrayList();

    @Setup
    public void setup() {
        pathSegments.add("path1");
        pathSegments.add("path2");
        pathSegments.add("path3");
        pathSegments.add("value1 value2");
        pathSegments.add("value1/value2");
        pathSegments.add("/value1");
    }

    @Benchmark
    public void current() {
        for (String pathSegment : pathSegments) {
            URIBuilder.encodePathSegment(pathSegment);
        }
    }
}
