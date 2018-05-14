package core.framework.mongo;

import com.mongodb.ReadPreference;
import org.bson.conversions.Bson;

/**
 * @author neo
 */
public final class MapReduce<T> {
    public Class<T> resultClass;
    public String mapFunction;
    public String reduceFunction;
    public Bson filter;
    public ReadPreference readPreference;
}
