package core.framework.search;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.function.Consumer;

/**
 * @author neo
 */
public class ForEach<T> {
    @Nullable
    public String index;
    public QueryBuilder query = QueryBuilders.matchAllQuery();
    public Duration scrollTimeout = Duration.ofMinutes(1);
    public Integer limit = 1000;
    public Consumer<T> consumer;
}
