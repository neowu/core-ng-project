package core.framework.api.search;

import org.elasticsearch.client.Client;

/**
 * @author neo
 */
public interface ElasticSearch {
    void createIndex(String index, String source);

    void createIndexTemplate(String name, String source);

    void flush(String index);

    Client client();
}
