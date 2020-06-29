package core.framework.module;

/**
 * @author neo
 */
public class TestLogConfig extends LogConfig {
    @Override
    public void appendToKafka(String uri) {
        appendToConsole();
    }
}
