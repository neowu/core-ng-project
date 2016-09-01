package core.framework.test.mongo;

import core.framework.api.mongo.Collection;
import core.framework.api.mongo.Field;
import core.framework.api.mongo.Id;
import core.framework.api.mongo.MongoEnumValue;
import org.bson.types.ObjectId;

import java.time.ZonedDateTime;

/**
 * @author neo
 */
@Collection(name = "entity")
public class TestMongoEntity {
    @Id
    public ObjectId id;

    @Field(name = "string_field")
    public String stringField;

    @Field(name = "enum_field")
    public TestEnum enumField;

    @Field(name = "zoned_date_time_field")
    public ZonedDateTime zonedDateTimeField;

    public enum TestEnum {
        @MongoEnumValue("VALUE1")
        VALUE1,
        @MongoEnumValue("VALUE2")
        VALUE2
    }
}
