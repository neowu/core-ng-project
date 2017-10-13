package core.framework.search;

import core.framework.util.Lists;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.sort.SortBuilder;

import java.util.List;

/**
 * @author neo
 */
public class SearchRequest {
    public final List<AbstractAggregationBuilder<?>> aggregations = Lists.newArrayList();
    public final List<SortBuilder<?>> sorts = Lists.newArrayList();
    public String index;
    public QueryBuilder query;
    public SearchType type;
    public Integer skip;
    public Integer limit;
}
