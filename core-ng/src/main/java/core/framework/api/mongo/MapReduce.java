package core.framework.api.mongo;

import com.mongodb.ReadPreference;
import org.bson.conversions.Bson;

/**
 * @author neo
 */
public class MapReduce<T> {
    public Class<T> resultClass;
    public String mapFunction;
    public String reduceFunction;
    public Bson filter;
    public ReadPreference readPreference;
}
