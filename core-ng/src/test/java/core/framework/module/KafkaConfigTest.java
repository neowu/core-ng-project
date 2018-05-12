package core.framework.module;

import core.framework.impl.inject.BeanFactory;
import core.framework.impl.module.ModuleContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class KafkaConfigTest {
    private ModuleContext context;

    @BeforeEach
    void createModuleContext() {
        context = new ModuleContext(new BeanFactory());
    }

    @Test
    void managementPathPattern() {
        KafkaConfig config = new KafkaConfig(context, null);
        assertThat(config.managementPathPattern("/topic/:topic")).isEqualTo("/_sys/kafka/topic/:topic");

        config = new KafkaConfig(context, "name");
        assertThat(config.managementPathPattern("/topic/:topic")).isEqualTo("/_sys/kafka/name/topic/:topic");
    }
}
