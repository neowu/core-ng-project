package core.framework.search;

/**
 * @author neo
 */
public interface ElasticSearch {
    void createIndex(String index, String source);

    void createIndexTemplate(String name, String source);

    void closeIndex(String index);

    void deleteIndex(String index);

    ClusterStateResponse state();

    void flushIndex(String index);
}
