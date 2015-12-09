package core.framework.api.crypto;

import core.framework.api.util.Lists;
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
public class HashBenchmark {
    private final List<String> values = Lists.newArrayList();

    @Setup
    public void setup() {
        values.add("abcdefghigklmnopqrstuvwxyz012345679");
        values.add("ABCDEFGHIGKLMNOPQRSTUVWXYZ012345679");
        values.add("012345679ABCDEFGHIGKLMNOPQRSTUVWXYZ");
        values.add("012345679abcdefghigklmnopqrstuvwxyz");
        values.add("012345679abcdefghigkLMNOPQRSTUvwxyz");
        values.add("012345679ABCDEFGHIGKlmnopqrstuVWXYZ");
    }

    @Benchmark
    public void current() {
        for (String value : values) {
            Hash.md5Hex(value);
        }
    }
}
