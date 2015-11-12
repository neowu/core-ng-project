package core.framework.test.mongo;

import core.framework.api.mongo.Collection;
import core.framework.api.mongo.Field;
import core.framework.api.mongo.Id;
import org.bson.types.ObjectId;

/**
 * @author neo
 */
@Collection(name = "entity")
public class TestEntity {
    @Id
    public ObjectId id;

    @Field(name = "string_field")
    public String stringField;
}
