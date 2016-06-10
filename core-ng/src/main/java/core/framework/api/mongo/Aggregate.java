package core.framework.api.mongo;

import com.mongodb.ReadPreference;
import org.bson.conversions.Bson;

import java.util.List;

/**
 * @author neo
 */
public class Aggregate<T> {
    public Class<T> resultClass;
    public List<Bson> pipeline;     // refer to com.mongodb.client.model.Aggregates
    public ReadPreference readPreference;
}
