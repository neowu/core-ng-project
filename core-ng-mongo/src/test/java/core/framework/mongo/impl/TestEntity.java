package core.framework.mongo.impl;

import core.framework.api.validate.NotNull;
import core.framework.mongo.Collection;
import core.framework.mongo.Field;
import core.framework.mongo.Id;
import org.bson.types.ObjectId;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
@Collection(name = "test_entity")
public class TestEntity {
    public static final String INT_FIELD = "int_field";

    @Id
    public ObjectId id;

    @NotNull
    @Field(name = INT_FIELD)
    public Integer intField;

    @Field(name = "double_field")
    public Double doubleField;

    @Field(name = "big_decimal_field")
    public BigDecimal bigDecimalField;

    @Field(name = "long_field")
    public Long longField;

    @Field(name = "boolean_field")
    public Boolean booleanField;

    @Field(name = "date_time_field")
    public LocalDateTime dateTimeField;

    @Field(name = "zoned_date_time_field")
    public ZonedDateTime zonedDateTimeField;

    @Field(name = "date_field")
    public LocalDate dateField;

    @Field(name = "string_field")
    public String stringField;

    @Field(name = "list_field")
    public List<String> listField;

    @Field(name = "map_field")
    public Map<String, String> mapField;

    @Field(name = "child")
    public TestChildEntity child;

    @Field(name = "children")
    public List<TestChildEntity> children;

    @Field(name = "children_map")
    public Map<String, TestChildEntity> childrenMap;

    @Field(name = "null_child")
    public TestChildEntity nullChild;

    @Field(name = "enum_map_field")
    public Map<TestEnum, String> enumMapField;

    @Field(name = "map_list_field")
    public Map<String, List<String>> mapListField;
}
