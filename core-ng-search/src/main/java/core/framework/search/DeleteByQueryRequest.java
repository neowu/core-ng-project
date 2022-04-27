package core.framework.search;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;

import javax.annotation.Nullable;

/**
 * @author neo
 */
public class DeleteByQueryRequest {
    @Nullable
    public String index;
    public Query query;
    @Nullable
    public Long batchSize;  // scroll size, default is 1000, refer to https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-delete-by-query.html
    @Nullable
    public Long limits; // limit max docs to delete
    @Nullable
    public Boolean refresh; // whether refresh index after operation, by default, changes only visible after index settings->refresh_interval
}
