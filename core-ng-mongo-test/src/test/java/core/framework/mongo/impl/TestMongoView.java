package core.framework.mongo.impl;

import core.framework.mongo.Field;

/**
 * @author neo
 */
public class TestMongoView {
    @Field(name = "_id")
    public String id;

    @Field(name = "string_field")
    public String stringField;
}
