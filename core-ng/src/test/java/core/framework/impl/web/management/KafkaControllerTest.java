package core.framework.impl.web.management;

import core.framework.util.Lists;
import org.apache.kafka.common.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class KafkaControllerTest {
    private KafkaController controller;

    @BeforeEach
    void createKafkaController() {
        controller = new KafkaController(null);
    }

    @Test
    void nodes() {
        List<Node> nodes = Lists.newArrayList(new Node(1001, "kafka-0", 9092), new Node(1002, "kafka-1", 9092));
        assertEquals("kafka-0:9092(1001), kafka-1:9092(1002)", controller.nodes(nodes));
    }
}
