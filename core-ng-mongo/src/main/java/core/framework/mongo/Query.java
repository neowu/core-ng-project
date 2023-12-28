package core.framework.mongo;

import com.mongodb.ReadPreference;
import org.bson.conversions.Bson;

/**
 * @author neo
 */
public final class Query {
    // refer to com.mongodb.client.model.Filters
    public Bson filter;
    // refer to com.mongodb.client.model.Projections
    public Bson projection;
    // refer to com.mongodb.client.model.Sorts
    public Bson sort;
    public Integer skip;
    public Integer limit;
    public ReadPreference readPreference;
}
