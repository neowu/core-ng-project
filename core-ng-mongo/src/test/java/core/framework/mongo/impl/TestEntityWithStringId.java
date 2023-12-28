package core.framework.mongo.impl;

import core.framework.mongo.Collection;
import core.framework.mongo.Field;
import core.framework.mongo.Id;

/**
 * @author neo
 */
@Collection(name = "test_entity_with_string_id")
public class TestEntityWithStringId {
    @Id
    public String id;

    @Field(name = "string_field")
    public String stringField;
}
