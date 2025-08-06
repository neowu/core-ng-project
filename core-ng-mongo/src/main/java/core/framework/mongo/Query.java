package core.framework.mongo;

import com.mongodb.ReadPreference;
import org.bson.conversions.Bson;
import org.jspecify.annotations.Nullable;

/**
 * @author neo
 */
public final class Query {
    // refer to com.mongodb.client.model.Filters
    @Nullable
    public Bson filter;
    // refer to com.mongodb.client.model.Projections
    @Nullable
    public Bson projection;
    // refer to com.mongodb.client.model.Sorts
    @Nullable
    public Bson sort;
    @Nullable
    public Integer skip;
    @Nullable
    public Integer limit;
    @Nullable
    public ReadPreference readPreference;
}
