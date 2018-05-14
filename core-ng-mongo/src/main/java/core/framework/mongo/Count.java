package core.framework.mongo;

import com.mongodb.ReadPreference;
import org.bson.conversions.Bson;

/**
 * @author neo
 */
public final class Count {
    public Bson filter;
    public ReadPreference readPreference;
}
