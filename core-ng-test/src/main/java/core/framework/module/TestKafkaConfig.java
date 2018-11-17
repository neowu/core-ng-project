package core.framework.module;

import core.framework.internal.kafka.MessageProducer;
import core.framework.test.kafka.MockMessageProducer;

/**
 * @author neo
 */
public class TestKafkaConfig extends KafkaConfig {
    @Override
    MessageProducer createProducer() {
        return new MockMessageProducer();
    }
}
