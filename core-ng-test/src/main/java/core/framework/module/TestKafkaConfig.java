package core.framework.module;

import core.framework.impl.kafka.Kafka;
import core.framework.impl.module.ModuleContext;
import core.framework.test.kafka.MockKafka;

/**
 * @author neo
 */
public class TestKafkaConfig extends KafkaConfig {
    TestKafkaConfig(ModuleContext context, String name) {
        super(context, name);
    }

    @Override
    Kafka createKafka(ModuleContext context, String name) {
        return new MockKafka();
    }
}