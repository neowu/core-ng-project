package core.log;

import core.framework.api.AbstractTestModule;
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
        overrideBinding(KafkaConsumerFactory.class, null, new KafkaConsumerFactory("localhost:9092") {
            @Override
            public Consumer<String, byte[]> create() {
                return new MockConsumer<>(OffsetResetStrategy.EARLIEST);
            }
        });

        load(new LogProcessorApp());

        initSearch().createIndexTemplate("action", "action-index-template.json");
        initSearch().createIndexTemplate("trace", "trace-index-template.json");
        initSearch().createIndexTemplate("stat", "stat-index-template.json");
    }
}
