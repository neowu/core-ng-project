package core.framework.module;

import core.framework.impl.module.ModuleContext;

/**
 * @author neo
 */
public class TestLogConfig extends LogConfig {
    TestLogConfig(ModuleContext context) {
        super(context);
    }

    @Override
    public void writeToKafka(String kafkaURI) {
        writeToConsole();
    }
}
