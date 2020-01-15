package core.framework.module;

import core.framework.internal.kafka.MessageProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

/**
 * @author neo
 */
class KafkaConfigTest {
    private KafkaConfig config;

    @BeforeEach
    void createKafkaConfig() {
        config = new KafkaConfig();
    }

    @Test
    void managementPathPattern() {
        assertThat(config.managementPathPattern("/topic/:topic")).isEqualTo("/_sys/kafka/topic/:topic");

        config.name = "name";
        assertThat(config.managementPathPattern("/topic/:topic")).isEqualTo("/_sys/kafka/name/topic/:topic");
    }

    @Test
    void maxRequestSize() {
        assertThatThrownBy(() -> config.maxRequestSize(0))
                .isInstanceOf(Error.class)
                .hasMessageContaining("max request size must be greater than 0");

        config.producer = mock(MessageProducer.class);

        assertThatThrownBy(() -> config.maxRequestSize(100))
                .isInstanceOf(Error.class)
                .hasMessageContaining("must be configured before adding publisher");
    }

    @Test
    void validate() {
        assertThatThrownBy(() -> config.validate())
                .hasMessageContaining("no producer/consumer added");
    }
}
