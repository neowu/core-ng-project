package core.framework.impl.web.management;

import core.framework.impl.kafka.Kafka;
import core.framework.impl.web.http.IPAccessControl;
import core.framework.json.JSON;
import core.framework.util.Lists;
import core.framework.util.Maps;
import core.framework.util.Strings;
import core.framework.web.Request;
import core.framework.web.Response;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.NewPartitions;
import org.apache.kafka.clients.admin.RecordsToDelete;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * @author neo
 */
public class KafkaController {
    private final Logger logger = LoggerFactory.getLogger(KafkaController.class);
    private final IPAccessControl accessControl = new IPAccessControl();
    private final Kafka kafka;

    public KafkaController(Kafka kafka) {
        this.kafka = kafka;
    }

    public Response topics(Request request) throws ExecutionException, InterruptedException {
        accessControl.validate(request.clientIP());
        List<KafkaTopic> views = Lists.newArrayList();
        AdminClient admin = kafka.admin();
        Set<String> topics = admin.listTopics().names().get();
        DescribeTopicsResult descriptions = admin.describeTopics(topics);
        for (Map.Entry<String, KafkaFuture<TopicDescription>> entry : descriptions.values().entrySet()) {
            String name = entry.getKey();
            TopicDescription description = entry.getValue().get();
            KafkaTopic view = view(name, description);
            views.add(view);
        }
        return Response.bean(views);
    }

    public Response updateTopic(Request request) {
        accessControl.validate(request.clientIP());
        String topic = request.pathParam("topic");
        UpdateTopicRequest updateTopicRequest = request.bean(UpdateTopicRequest.class);
        updateTopic(topic, updateTopicRequest);
        return Response.text(Strings.format("update topic request submitted, topic={}, request={}", topic, JSON.toJSON(updateTopicRequest)));
    }

    void updateTopic(String topic, UpdateTopicRequest request) {
        AdminClient admin = kafka.admin();
        if (request.partitions != null) {
            Map<String, NewPartitions> partitions = Maps.newHashMap();
            partitions.put(topic, NewPartitions.increaseTo(request.partitions));
            logger.info("update kafka partitions, topic={}, increaseTo={}", topic, request.partitions);
            admin.createPartitions(partitions);
        }
        if (request.deleteRecords != null) {
            Map<TopicPartition, RecordsToDelete> records = Maps.newHashMap();
            for (UpdateTopicRequest.DeleteRecord deleteRecord : request.deleteRecords) {
                records.put(new TopicPartition(topic, deleteRecord.partition), RecordsToDelete.beforeOffset(deleteRecord.beforeOffset));
            }
            logger.info("delete kafka records, topic={}, records={}", topic, JSON.toJSON(request.deleteRecords));
            admin.deleteRecords(records);
        }
    }

    private KafkaTopic view(String name, TopicDescription description) {
        KafkaTopic view = new KafkaTopic();
        view.name = name;
        for (TopicPartitionInfo info : description.partitions()) {
            KafkaTopic.Partition partition = new KafkaTopic.Partition();
            partition.id = info.partition();
            partition.leader = node(info.leader());
            partition.replicas = nodes(info.replicas());
            partition.inSyncReplicas = nodes(info.isr());
            view.partitions.add(partition);
        }
        return view;
    }

    String nodes(List<Node> nodes) {
        return nodes.stream().map(this::node).collect(Collectors.joining(", "));
    }

    private String node(Node node) {
        return node.host() + ":" + node.port() + "(" + node.id() + ")";
    }
}
