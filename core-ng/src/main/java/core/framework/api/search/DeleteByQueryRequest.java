package core.framework.api.search;

import org.elasticsearch.index.query.QueryBuilder;

/**
 * @author neo
 */
public class DeleteByQueryRequest {
    public String index;
    public QueryBuilder query;
}
