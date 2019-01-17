package core.framework.mongo.impl;

import core.framework.mongo.Collection;
import core.framework.mongo.Field;
import core.framework.mongo.Id;
import core.framework.mongo.MongoEnumValue;
import org.bson.types.ObjectId;

import java.time.ZonedDateTime;
import java.util.Map;

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

    @Field(name = "enum_map_field")
    public Map<TestEnum, String> enumMapField;

    public enum TestEnum {
        @MongoEnumValue("V1")
        VALUE1,
        @MongoEnumValue("V2")
        VALUE2
    }
}
