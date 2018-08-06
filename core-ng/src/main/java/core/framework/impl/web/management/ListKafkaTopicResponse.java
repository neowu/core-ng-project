package core.framework.impl.web.management;

import core.framework.api.json.Property;
import core.framework.api.validate.NotNull;
import core.framework.util.Lists;

import java.util.ArrayList;
import java.util.List;

/**
 * @author neo
 */
public class ListKafkaTopicResponse {
    @NotNull
    @Property(name = "topics")
    public List<KafkaTopic> topics = new ArrayList<>();

    public static class KafkaTopic {
        @NotNull
        @Property(name = "name")
        public String name;

        @NotNull
        @Property(name = "partitions")
        public List<Partition> partitions = Lists.newArrayList();

    }
    public static class Partition {
        @NotNull
        @Property(name = "id")
        public Integer id;

        @Property(name = "leader")
        public String leader;

        @Property(name = "replicas")
        public String replicas;

        @Property(name = "in_sync_replicas")
        public String inSyncReplicas;
    }
}
