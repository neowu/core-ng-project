package core.framework.api.mongo;

import com.mongodb.ReadPreference;
import org.bson.conversions.Bson;

/**
 * @author neo
 */
public final class FindOne {
    public Bson filter;
    public ReadPreference readPreference;
}
