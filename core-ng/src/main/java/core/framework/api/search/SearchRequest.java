package core.framework.api.search;

import core.framework.api.util.Lists;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.sort.SortBuilder;

import java.util.List;

/**
 * @author neo
 */
public class SearchRequest {
    public final List<AbstractAggregationBuilder> aggregations = Lists.newArrayList();
    public final List<SortBuilder> sorts = Lists.newArrayList();
    public String index;
    public QueryBuilder query;
    public Integer skip;
    public Integer limit;
}
