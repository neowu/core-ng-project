package core.framework.impl.web.management;

import core.framework.api.json.Property;
import core.framework.util.Lists;

import java.util.List;

/**
 * @author neo
 */
public class KafkaTopic {
    @Property(name = "name")
    public String name;
    @Property(name = "partitions")
    public List<Partition> partitions = Lists.newArrayList();

    public static class Partition {
        @Property(name = "id")
        public Integer id;

        @Property(name = "leader")
        public String leader;

        @Property(name = "replicas")
        public String replicas;

        @Property(name = "in-sync-replicas")
        public String inSyncReplicas;
    }
}
