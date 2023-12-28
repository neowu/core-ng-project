package core.framework.mongo;

import com.mongodb.ReadPreference;
import org.bson.conversions.Bson;

/**
 * @author neo
 */
public final class FindOne {
    // refer to com.mongodb.client.model.Filters
    public Bson filter;
    public ReadPreference readPreference;
}
