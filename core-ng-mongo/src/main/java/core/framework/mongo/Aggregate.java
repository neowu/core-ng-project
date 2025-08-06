package core.framework.mongo;

import com.mongodb.ReadPreference;
import org.bson.conversions.Bson;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * @author neo
 */
public final class Aggregate<T> {
    // refer to com.mongodb.client.model.Aggregates
    public List<Bson> pipeline;
    public Class<T> resultClass;
    @Nullable
    public ReadPreference readPreference;
}
