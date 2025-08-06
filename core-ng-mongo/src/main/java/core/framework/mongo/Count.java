package core.framework.mongo;

import com.mongodb.ReadPreference;
import org.bson.conversions.Bson;
import org.jspecify.annotations.Nullable;

/**
 * @author neo
 */
public final class Count {
    @Nullable
    public Bson filter;
    @Nullable
    public ReadPreference readPreference;
}
