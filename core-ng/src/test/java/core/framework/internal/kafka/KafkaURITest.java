package core.framework.internal.kafka;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class KafkaURITest {
    @Test
    void parse() {
        assertThat(KafkaURI.parse("kafka-0.kafka"))
                .containsExactly("kafka-0.kafka:9092");

        assertThat(KafkaURI.parse("kafka-0.kafka:9092"))
                .containsExactly("kafka-0.kafka:9092");

        assertThat(KafkaURI.parse("kafka-0.kafka, kafka-1.kafka:9092"))
                .containsExactly("kafka-0.kafka:9092", "kafka-1.kafka:9092");
    }
}
