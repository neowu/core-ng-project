package core.framework.impl.mongo;

import core.framework.mongo.Field;

/**
 * @author neo
 */
public class TestView {
    @Field(name = "_id")
    public String id;
    @Field(name = "int_field")
    public Integer intField;
}
