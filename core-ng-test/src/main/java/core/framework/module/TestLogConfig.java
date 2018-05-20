package core.framework.module;

/**
 * @author neo
 */
public class TestLogConfig extends LogConfig {
    @Override
    public void writeToKafka(String kafkaURI) {
        writeToConsole();
    }
}
