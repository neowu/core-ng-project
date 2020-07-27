package core.framework.search;

import core.framework.util.Lists;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.sort.SortBuilder;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author neo
 */
public class SearchRequest {
    public final List<AbstractAggregationBuilder<?>> aggregations = Lists.newArrayList();
    public final List<SortBuilder<?>> sorts = Lists.newArrayList();
    @Nullable
    public String index;
    public QueryBuilder query;
    @Nullable
    public SearchType type;
    @Nullable
    public Integer skip;
    @Nullable
    public Integer limit;
    @Nullable
    public Integer trackTotalHitsUpTo;

    public void trackTotalHits() {
        trackTotalHitsUpTo = Integer.MAX_VALUE;
    }
}
