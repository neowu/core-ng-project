package core.framework.module;

/**
 * @author neo
 */
public class TestLogConfig extends LogConfig {
    @Override
    public void toKafka(String kafkaURI) {
        toConsole();
    }
}
