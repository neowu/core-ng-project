package core.framework.search;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;

import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public class SearchResponse<T> {
    public final List<T> hits;
    public final long totalHits;
    public final Map<String, Aggregate> aggregations;

    public SearchResponse(List<T> hits, long totalHits, Map<String, Aggregate> aggregations) {
        this.hits = hits;
        this.totalHits = totalHits;
        this.aggregations = aggregations;
    }
}
