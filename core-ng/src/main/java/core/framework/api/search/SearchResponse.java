package core.framework.api.search;

import org.elasticsearch.search.aggregations.Aggregation;

import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public class SearchResponse<T> {
    public final List<T> hits;
    public final long totalHits;
    public final Map<String, Aggregation> aggregations;

    public SearchResponse(List<T> hits, long totalHits, Map<String, Aggregation> aggregations) {
        this.hits = hits;
        this.totalHits = totalHits;
        this.aggregations = aggregations;
    }
}
