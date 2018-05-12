package core.framework.search;

import org.elasticsearch.cluster.metadata.IndexMetaData;

/**
 * @author neo
 */
public class ElasticSearchIndex {
    public String index;
    public IndexMetaData.State state;
}
