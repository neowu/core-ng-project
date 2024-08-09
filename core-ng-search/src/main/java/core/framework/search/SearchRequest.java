package core.framework.search;

import co.elastic.clients.elasticsearch._types.SearchType;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.mapping.RuntimeField;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import core.framework.util.Lists;
import core.framework.util.Maps;

import javax.annotation.Nullable;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public class SearchRequest {
    public final Map<String, Aggregation> aggregations = Maps.newHashMap();
    public final Map<String, RuntimeField> runtimeFields = Maps.newHashMap();
    public final List<SortOptions> sorts = Lists.newArrayList();
    @Nullable
    public String index;
    public Query query;
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

    public void withJSON(String source) {
        var request = co.elastic.clients.elasticsearch.core.SearchRequest.of(b -> b.withJson(new StringReader(source)));
        if (request.query() != null) query = request.query();
        if (request.aggregations() != null) aggregations.putAll(request.aggregations());
        if (request.runtimeMappings() != null) runtimeFields.putAll(request.runtimeMappings());
        if (request.sort() != null) sorts.addAll(request.sort());
    }
}
