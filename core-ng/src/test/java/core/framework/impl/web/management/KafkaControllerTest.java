package core.framework.impl.web.management;

import core.framework.impl.kafka.Kafka;
import core.framework.impl.kafka.KafkaHeaders;
import core.framework.util.Lists;
import core.framework.util.Strings;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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
        controller = new KafkaController(kafka);
    }

    @Test
    void nodes() {
        List<Node> nodes = Lists.newArrayList(new Node(1001, "kafka-0", 9092), new Node(1002, "kafka-1", 9092));
        assertThat(controller.nodes(nodes)).isEqualTo("kafka-0:9092(1001), kafka-1:9092(1002)");
    }

    @Test
    void updateTopic() {
        UpdateTopicRequest request = new UpdateTopicRequest();
        request.partitions = 10;
        request.deleteRecords = Lists.newArrayList();
        UpdateTopicRequest.DeleteRecord deleteRecord = new UpdateTopicRequest.DeleteRecord();
        deleteRecord.partition = 1;
        deleteRecord.beforeOffset = 1000L;
        request.deleteRecords.add(deleteRecord);
        controller.updateTopic("topic", request);

        verify(adminClient).createPartitions(argThat(partitions -> partitions.get("topic").totalCount() == 10));
        verify(adminClient).deleteRecords(argThat(records -> records.get(new TopicPartition("topic", 1)).beforeOffset() == 1000));
    }

    @Test
    void record() {
        ProducerRecord<String, byte[]> record = controller.record("topic", "key", new byte[0]);
        assertThat(record.headers().lastHeader(KafkaHeaders.HEADER_TRACE).value()).isEqualTo(Strings.bytes("true"));
    }
}
