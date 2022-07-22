package core.framework.search;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.function.Consumer;

/**
 * @author neo
 */
public class ForEach<T> {
    @Nullable
    public String index;
    public Query query = new Query.Builder().matchAll(m -> m).build();
    public Duration scrollTimeout = Duration.ofMinutes(1);
    public Integer batchSize = 1000;
    public Consumer<T> consumer;
}
