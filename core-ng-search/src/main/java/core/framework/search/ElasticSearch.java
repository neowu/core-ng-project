package core.framework.search;

import java.util.List;

/**
 * @author neo
 */
public interface ElasticSearch {
    void createIndex(String index, String source);

    void createIndexTemplate(String name, String source);

    void closeIndex(String index);

    void deleteIndex(String index);

    List<ElasticSearchIndex> indices();

    void flush(String index);
}
