package app.domain;

import core.framework.api.mongo.Field;

/**
 * @author neo
 */
public class MongoUserAggregateView {
    @Field(name = "_id")
    public String id;

    @Field(name = "total")
    public Integer total;
}
