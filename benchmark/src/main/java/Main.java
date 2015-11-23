import core.framework.api.util.URIBuilderEncodePathSegmentBenchmark;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * @author neo
 */
public class Main {
    public static void main(String[] args) throws RunnerException {
        ChainedOptionsBuilder builder = new OptionsBuilder()
            .include(URIBuilderEncodePathSegmentBenchmark.class.getSimpleName())
            .forks(2)
            .warmupIterations(4)
            .measurementIterations(10);

//        builder.addProfiler(StackProfiler.class)
//            .jvmArgsAppend("-Djmh.stack.lines=3");

        Options options = builder.build();
        new Runner(options).run();
    }
}
