package core.log;

import core.framework.test.module.AbstractTestModule;
import core.log.service.IndexService;
import core.log.service.KafkaConsumerFactory;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;

/**
 * @author neo
 */
public class TestModule extends AbstractTestModule {
    @Override
    protected void initialize() {
        overrideBinding(KafkaConsumerFactory.class, null, new MockKafkaConsumerFactory());

        load(new LogProcessorApp());

        bean(IndexService.class).createIndexTemplates();
    }

    private static class MockKafkaConsumerFactory extends KafkaConsumerFactory {
        MockKafkaConsumerFactory() {
            super(null);
        }

        @Override
        public Consumer<String, byte[]> create() {
            return new MockConsumer<>(OffsetResetStrategy.EARLIEST);
        }
    }
}
