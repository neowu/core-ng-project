package core.framework.search;

/**
 * @author neo
 */
public interface ElasticSearch {
    // create index or update mappings
    void putIndex(String index, String source);

    void putIndexTemplate(String name, String source);

    void closeIndex(String index);

    void deleteIndex(String index);

    ClusterStateResponse state();

    void refreshIndex(String index);
}
