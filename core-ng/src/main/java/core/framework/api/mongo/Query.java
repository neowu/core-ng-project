package core.framework.api.mongo;

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
}
