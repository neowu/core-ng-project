package core.framework.module;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
}
