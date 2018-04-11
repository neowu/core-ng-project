package core.framework.impl.web.management;

import core.framework.impl.kafka.Kafka;
import core.framework.util.Lists;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

/**
 * @author neo
 */
class KafkaControllerTest {
    private KafkaController controller;
    private AdminClient adminClient;

    @BeforeEach
    void createKafkaController() {
        Kafka kafka = Mockito.mock(Kafka.class);
        adminClient = Mockito.mock(AdminClient.class);
        Mockito.when(kafka.admin()).thenReturn(adminClient);
        controller = new KafkaController(kafka, null);
    }

    @Test
    void nodes() {
        List<Node> nodes = Lists.newArrayList(new Node(1001, "kafka-0", 9092), new Node(1002, "kafka-1", 9092));
        assertEquals("kafka-0:9092(1001), kafka-1:9092(1002)", controller.nodes(nodes));
    }

    @Test
    void updateTopic() {
        UpdateTopicRequest request = new UpdateTopicRequest();
        request.partitions = 10;
        controller.updateTopic("topic", request);

        verify(adminClient).createPartitions(argThat(partitions -> partitions.get("topic").totalCount() == 10));
        verify(adminClient).close();
    }

    @Test
    void deleteRecords() {
        DeleteRecordRequest request = new DeleteRecordRequest();
        request.partition = 1;
        request.offset = 1000L;
        controller.deleteRecords("topic", request);

        verify(adminClient).deleteRecords(argThat(records -> records.get(new TopicPartition("topic", 1)).beforeOffset() == 1000));
        verify(adminClient).close();
    }
}
