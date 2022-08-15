public class EntityDecoder$TestEntity implements core.framework.mongo.impl.EntityDecoder {
    private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(core.framework.mongo.impl.EntityDecoder.class);

    private final core.framework.mongo.impl.EnumCodec enumCodecTestEnum21 = new core.framework.mongo.impl.EnumCodec(core.framework.mongo.impl.TestEnum.class);

    private java.util.List decodeListString12(org.bson.BsonReader reader, core.framework.mongo.impl.BsonReaderWrapper wrapper, String fieldPath) {
        java.util.List list = wrapper.startReadList(fieldPath);
        if (list == null) return null;
        reader.readStartArray();
        while (reader.readBsonType() != org.bson.BsonType.END_OF_DOCUMENT) {
            java.lang.String $13 = wrapper.readString(fieldPath);
            list.add($13);
        }
        reader.readEndArray();
        return list;
    }

    private java.util.Map decodeMapStringString15(org.bson.BsonReader reader, core.framework.mongo.impl.BsonReaderWrapper wrapper, String parentField) {
        java.util.Map map = wrapper.startReadMap(parentField);
        if (map == null) return null;
        reader.readStartDocument();
        while (reader.readBsonType() != org.bson.BsonType.END_OF_DOCUMENT) {
            String fieldName = reader.readName();
            String fieldPath = parentField + "." + fieldName;
            java.lang.String $16 = wrapper.readString(fieldPath);
            map.put(fieldName, $16);
        }
        reader.readEndDocument();
        return map;
    }

    private java.util.List decodeListTestEnum23(org.bson.BsonReader reader, core.framework.mongo.impl.BsonReaderWrapper wrapper, String fieldPath) {
        java.util.List list = wrapper.startReadList(fieldPath);
        if (list == null) return null;
        reader.readStartArray();
        while (reader.readBsonType() != org.bson.BsonType.END_OF_DOCUMENT) {
            core.framework.mongo.impl.TestEnum $24 = (core.framework.mongo.impl.TestEnum) enumCodecTestEnum21.read(reader, fieldPath);
            list.add($24);
        }
        reader.readEndArray();
        return list;
    }

    public core.framework.mongo.impl.TestChildEntity decodeTestChildEntity18(org.bson.BsonReader reader, core.framework.mongo.impl.BsonReaderWrapper wrapper, String parentField) {
        boolean hasContent = wrapper.startReadEntity(parentField);
        if (!hasContent) return null;
        core.framework.mongo.impl.TestChildEntity entity = new core.framework.mongo.impl.TestChildEntity();
        reader.readStartDocument();
        while (reader.readBsonType() != org.bson.BsonType.END_OF_DOCUMENT) {
            String fieldName = reader.readName();
            String fieldPath = parentField + "." + fieldName;
            switch (fieldName) {
                case "boolean_field": {
                    java.lang.Boolean $19 = wrapper.readBoolean(fieldPath);
                    entity.booleanField = $19;
                    continue;
                }
                case "enum_field": {
                    core.framework.mongo.impl.TestEnum $20 = (core.framework.mongo.impl.TestEnum) enumCodecTestEnum21.read(reader, fieldPath);
                    entity.enumField = $20;
                    continue;
                }
                case "enum_list_field": {
                    java.util.List $22 = decodeListTestEnum23(reader, wrapper, fieldPath);
                    entity.enumListField = $22;
                    continue;
                }
                case "ref_id_field": {
                    org.bson.types.ObjectId $25 = wrapper.readObjectId(fieldPath);
                    entity.refId = $25;
                    continue;
                }
            }
            logger.warn("undefined field, field={}, type={}", fieldPath, reader.getCurrentBsonType());
            reader.skipValue();
        }
        reader.readEndDocument();
        return entity;
    }

    private java.util.List decodeListTestChildEntity27(org.bson.BsonReader reader, core.framework.mongo.impl.BsonReaderWrapper wrapper, String fieldPath) {
        java.util.List list = wrapper.startReadList(fieldPath);
        if (list == null) return null;
        reader.readStartArray();
        while (reader.readBsonType() != org.bson.BsonType.END_OF_DOCUMENT) {
            core.framework.mongo.impl.TestChildEntity $28 = decodeTestChildEntity18(reader, wrapper, fieldPath);
            list.add($28);
        }
        reader.readEndArray();
        return list;
    }

    private java.util.Map decodeMapStringTestChildEntity30(org.bson.BsonReader reader, core.framework.mongo.impl.BsonReaderWrapper wrapper, String parentField) {
        java.util.Map map = wrapper.startReadMap(parentField);
        if (map == null) return null;
        reader.readStartDocument();
        while (reader.readBsonType() != org.bson.BsonType.END_OF_DOCUMENT) {
            String fieldName = reader.readName();
            String fieldPath = parentField + "." + fieldName;
            core.framework.mongo.impl.TestChildEntity $31 = decodeTestChildEntity18(reader, wrapper, fieldPath);
            map.put(fieldName, $31);
        }
        reader.readEndDocument();
        return map;
    }

    private java.util.Map decodeMapTestEnumString34(org.bson.BsonReader reader, core.framework.mongo.impl.BsonReaderWrapper wrapper, String parentField) {
        java.util.Map map = wrapper.startReadMap(parentField);
        if (map == null) return null;
        reader.readStartDocument();
        while (reader.readBsonType() != org.bson.BsonType.END_OF_DOCUMENT) {
            String fieldName = reader.readName();
            String fieldPath = parentField + "." + fieldName;
            java.lang.String $35 = wrapper.readString(fieldPath);
            map.put(enumCodecTestEnum21.decodeMapKey(fieldName), $35);
        }
        reader.readEndDocument();
        return map;
    }

    private java.util.Map decodeMapStringList37(org.bson.BsonReader reader, core.framework.mongo.impl.BsonReaderWrapper wrapper, String parentField) {
        java.util.Map map = wrapper.startReadMap(parentField);
        if (map == null) return null;
        reader.readStartDocument();
        while (reader.readBsonType() != org.bson.BsonType.END_OF_DOCUMENT) {
            String fieldName = reader.readName();
            String fieldPath = parentField + "." + fieldName;
            java.util.List $38 = decodeListString12(reader, wrapper, fieldPath);
            map.put(fieldName, $38);
        }
        reader.readEndDocument();
        return map;
    }

    public core.framework.mongo.impl.TestEntity decodeTestEntity0(org.bson.BsonReader reader, core.framework.mongo.impl.BsonReaderWrapper wrapper, String parentField) {
        boolean hasContent = wrapper.startReadEntity(parentField);
        if (!hasContent) return null;
        core.framework.mongo.impl.TestEntity entity = new core.framework.mongo.impl.TestEntity();
        reader.readStartDocument();
        while (reader.readBsonType() != org.bson.BsonType.END_OF_DOCUMENT) {
            String fieldName = reader.readName();
            String fieldPath = parentField + "." + fieldName;
            switch (fieldName) {
                case "_id": {
                    org.bson.types.ObjectId $1 = wrapper.readObjectId(fieldPath);
                    entity.id = $1;
                    continue;
                }
                case "int_field": {
                    java.lang.Integer $2 = wrapper.readInteger(fieldPath);
                    entity.intField = $2;
                    continue;
                }
                case "double_field": {
                    java.lang.Double $3 = wrapper.readDouble(fieldPath);
                    entity.doubleField = $3;
                    continue;
                }
                case "big_decimal_field": {
                    java.math.BigDecimal $4 = wrapper.readBigDecimal(fieldPath);
                    entity.bigDecimalField = $4;
                    continue;
                }
                case "long_field": {
                    java.lang.Long $5 = wrapper.readLong(fieldPath);
                    entity.longField = $5;
                    continue;
                }
                case "boolean_field": {
                    java.lang.Boolean $6 = wrapper.readBoolean(fieldPath);
                    entity.booleanField = $6;
                    continue;
                }
                case "date_time_field": {
                    java.time.LocalDateTime $7 = wrapper.readLocalDateTime(fieldPath);
                    entity.dateTimeField = $7;
                    continue;
                }
                case "zoned_date_time_field": {
                    java.time.ZonedDateTime $8 = wrapper.readZonedDateTime(fieldPath);
                    entity.zonedDateTimeField = $8;
                    continue;
                }
                case "date_field": {
                    java.time.LocalDate $9 = wrapper.readLocalDate(fieldPath);
                    entity.dateField = $9;
                    continue;
                }
                case "string_field": {
                    java.lang.String $10 = wrapper.readString(fieldPath);
                    entity.stringField = $10;
                    continue;
                }
                case "list_field": {
                    java.util.List $11 = decodeListString12(reader, wrapper, fieldPath);
                    entity.listField = $11;
                    continue;
                }
                case "map_field": {
                    java.util.Map $14 = decodeMapStringString15(reader, wrapper, fieldPath);
                    entity.mapField = $14;
                    continue;
                }
                case "child": {
                    core.framework.mongo.impl.TestChildEntity $17 = decodeTestChildEntity18(reader, wrapper, fieldPath);
                    entity.child = $17;
                    continue;
                }
                case "children": {
                    java.util.List $26 = decodeListTestChildEntity27(reader, wrapper, fieldPath);
                    entity.children = $26;
                    continue;
                }
                case "children_map": {
                    java.util.Map $29 = decodeMapStringTestChildEntity30(reader, wrapper, fieldPath);
                    entity.childrenMap = $29;
                    continue;
                }
                case "null_child": {
                    core.framework.mongo.impl.TestChildEntity $32 = decodeTestChildEntity18(reader, wrapper, fieldPath);
                    entity.nullChild = $32;
                    continue;
                }
                case "enum_map_field": {
                    java.util.Map $33 = decodeMapTestEnumString34(reader, wrapper, fieldPath);
                    entity.enumMapField = $33;
                    continue;
                }
                case "map_list_field": {
                    java.util.Map $36 = decodeMapStringList37(reader, wrapper, fieldPath);
                    entity.mapListField = $36;
                    continue;
                }
            }
            logger.warn("undefined field, field={}, type={}", fieldPath, reader.getCurrentBsonType());
            reader.skipValue();
        }
        reader.readEndDocument();
        return entity;
    }

    public Object decode(org.bson.BsonReader reader) {
        core.framework.mongo.impl.BsonReaderWrapper wrapper = new core.framework.mongo.impl.BsonReaderWrapper(reader);
        return decodeTestEntity0(reader, wrapper, "");
    }

}
