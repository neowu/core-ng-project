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
public class ASCIIToUpperCaseBenchmark {
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
            ASCII.toUpperCase(value);
        }
    }
}
