package core.framework.internal.kafka;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class KafkaURITest {
    @Test
    void parse() {
        assertThat(new KafkaURI("kafka-0.kafka").bootstrapURIs)
            .containsExactly("kafka-0.kafka:9092");

        assertThat(new KafkaURI("kafka-0.kafka:9092").bootstrapURIs)
            .containsExactly("kafka-0.kafka:9092");

        assertThat(new KafkaURI("kafka-0.kafka, kafka-1.kafka:9092").bootstrapURIs)
            .containsExactly("kafka-0.kafka:9092", "kafka-1.kafka:9092");
    }

    @Test
    void convertToString() {
        assertThat(String.valueOf(new KafkaURI("kafka"))).isEqualTo("kafka");
    }
}
