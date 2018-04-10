package core.framework.impl.web.management;

import core.framework.impl.kafka.Kafka;
import core.framework.impl.web.http.IPAccessControl;
import core.framework.util.Lists;
import core.framework.web.Request;
import core.framework.web.Response;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartitionInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * @author neo
 */
public class KafkaController {
    private final Kafka kafka;
    private final IPAccessControl accessControl;

    public KafkaController(Kafka kafka, IPAccessControl accessControl) {
        this.kafka = kafka;
        this.accessControl = accessControl;
    }

    public Response topics(Request request) throws ExecutionException, InterruptedException {
        accessControl.validateClientIP(request.clientIP());
        List<KafkaTopic> views = Lists.newArrayList();
        try (AdminClient admin = kafka.admin()) {
            Set<String> topics = admin.listTopics().names().get();
            DescribeTopicsResult descriptions = admin.describeTopics(topics);
            for (Map.Entry<String, KafkaFuture<TopicDescription>> entry : descriptions.values().entrySet()) {
                String name = entry.getKey();
                TopicDescription description = entry.getValue().get();
                KafkaTopic view = view(name, description);
                views.add(view);
            }
        }
        return Response.bean(views);
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
