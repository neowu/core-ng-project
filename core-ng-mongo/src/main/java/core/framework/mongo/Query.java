package core.framework.mongo;

import com.mongodb.ReadPreference;
import org.bson.conversions.Bson;

/**
 * @author neo
 */
public final class Query {
    public Bson filter;
    public Bson projection;
    public Bson sort;
    public Integer skip;
    public Integer limit;
    public ReadPreference readPreference;
}
