package app.monitor.job;

import core.framework.api.json.Property;

import java.util.Map;

/**
 * @author neo
 */
public class ElasticSearchNodeStats {
    @Property(name = "_nodes")
    public NodeStats stats;

    @Property(name = "nodes")
    public Map<String, Node> nodes;

    public static class Node {
        @Property(name = "name")
        public String name;
        @Property(name = "indices")
        public Indices indices;
        @Property(name = "jvm")
        public JVM jvm;
        @Property(name = "fs")
        public FS fs;
        @Property(name = "os")
        public OS os;
    }

    public static class Indices {
        @Property(name = "docs")
        public Docs docs;
    }

    public static class Docs {
        @Property(name = "count")
        public Long count;
        @Property(name = "deleted")
        public Long deleted;
    }

    public static class JVM {
        @Property(name = "mem")
        public Mem mem;
        @Property(name = "gc")
        public GC gc;
    }

    public static class Mem {
        @Property(name = "heap_used_in_bytes")
        public Long heapUsedInBytes;
        @Property(name = "heap_max_in_bytes")
        public Long heapMaxInBytes;
        @Property(name = "non_heap_used_in_bytes")
        public Long nonHeapUsedInBytes;
    }

    public static class GC {
        @Property(name = "collectors")
        public Map<String, Collector> collectors;
    }

    public static class Collector {
        @Property(name = "collection_count")
        public Long collectionCount;

        @Property(name = "collection_time_in_millis")
        public Long collectionTimeInMillis;
    }

    public static class FS {
        @Property(name = "total")
        public Total total;
    }

    public static class Total {
        @Property(name = "total_in_bytes")
        public Long totalInBytes;
        @Property(name = "free_in_bytes")
        public Long freeInBytes;
    }

    public static class OS {
        @Property(name = "cpu")
        public CPU cpu;
    }

    public static class CPU {
        @Property(name = "percent")
        public Integer percent;
    }

    public static class NodeStats {
        @Property(name = "total")
        public Integer total;
        @Property(name = "successful")
        public Integer successful;
        @Property(name = "failed")
        public Integer failed;
    }
}
