package core.framework.search;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.time.Duration;
import java.util.function.Consumer;

/**
 * @author neo
 */
public class ForEach<T> {
    public String index;
    public QueryBuilder query = QueryBuilders.matchAllQuery();
    public Duration scrollTimeout = Duration.ofMinutes(1);
    public Integer limit = 1000;
    public Consumer<T> consumer;
}
