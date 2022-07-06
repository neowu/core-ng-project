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
            if ("boolean_field".equals(fieldName)) {
                java.lang.Boolean $19 = wrapper.readBoolean(fieldPath);
                entity.booleanField = $19;
                continue;
            }
            if ("enum_field".equals(fieldName)) {
                core.framework.mongo.impl.TestEnum $20 = (core.framework.mongo.impl.TestEnum) enumCodecTestEnum21.read(reader, fieldPath);
                entity.enumField = $20;
                continue;
            }
            if ("enum_list_field".equals(fieldName)) {
                java.util.List $22 = decodeListTestEnum23(reader, wrapper, fieldPath);
                entity.enumListField = $22;
                continue;
            }
            if ("ref_id_field".equals(fieldName)) {
                org.bson.types.ObjectId $25 = wrapper.readObjectId(fieldPath);
                entity.refId = $25;
                continue;
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
            if ("_id".equals(fieldName)) {
                org.bson.types.ObjectId $1 = wrapper.readObjectId(fieldPath);
                entity.id = $1;
                continue;
            }
            if ("int_field".equals(fieldName)) {
                java.lang.Integer $2 = wrapper.readInteger(fieldPath);
                entity.intField = $2;
                continue;
            }
            if ("double_field".equals(fieldName)) {
                java.lang.Double $3 = wrapper.readDouble(fieldPath);
                entity.doubleField = $3;
                continue;
            }
            if ("big_decimal_field".equals(fieldName)) {
                java.math.BigDecimal $4 = wrapper.readBigDecimal(fieldPath);
                entity.bigDecimalField = $4;
                continue;
            }
            if ("long_field".equals(fieldName)) {
                java.lang.Long $5 = wrapper.readLong(fieldPath);
                entity.longField = $5;
                continue;
            }
            if ("boolean_field".equals(fieldName)) {
                java.lang.Boolean $6 = wrapper.readBoolean(fieldPath);
                entity.booleanField = $6;
                continue;
            }
            if ("date_time_field".equals(fieldName)) {
                java.time.LocalDateTime $7 = wrapper.readLocalDateTime(fieldPath);
                entity.dateTimeField = $7;
                continue;
            }
            if ("zoned_date_time_field".equals(fieldName)) {
                java.time.ZonedDateTime $8 = wrapper.readZonedDateTime(fieldPath);
                entity.zonedDateTimeField = $8;
                continue;
            }
            if ("date_field".equals(fieldName)) {
                java.time.LocalDate $9 = wrapper.readLocalDate(fieldPath);
                entity.dateField = $9;
                continue;
            }
            if ("string_field".equals(fieldName)) {
                java.lang.String $10 = wrapper.readString(fieldPath);
                entity.stringField = $10;
                continue;
            }
            if ("list_field".equals(fieldName)) {
                java.util.List $11 = decodeListString12(reader, wrapper, fieldPath);
                entity.listField = $11;
                continue;
            }
            if ("map_field".equals(fieldName)) {
                java.util.Map $14 = decodeMapStringString15(reader, wrapper, fieldPath);
                entity.mapField = $14;
                continue;
            }
            if ("child".equals(fieldName)) {
                core.framework.mongo.impl.TestChildEntity $17 = decodeTestChildEntity18(reader, wrapper, fieldPath);
                entity.child = $17;
                continue;
            }
            if ("children".equals(fieldName)) {
                java.util.List $26 = decodeListTestChildEntity27(reader, wrapper, fieldPath);
                entity.children = $26;
                continue;
            }
            if ("children_map".equals(fieldName)) {
                java.util.Map $29 = decodeMapStringTestChildEntity30(reader, wrapper, fieldPath);
                entity.childrenMap = $29;
                continue;
            }
            if ("null_child".equals(fieldName)) {
                core.framework.mongo.impl.TestChildEntity $32 = decodeTestChildEntity18(reader, wrapper, fieldPath);
                entity.nullChild = $32;
                continue;
            }
            if ("enum_map_field".equals(fieldName)) {
                java.util.Map $33 = decodeMapTestEnumString34(reader, wrapper, fieldPath);
                entity.enumMapField = $33;
                continue;
            }
            if ("map_list_field".equals(fieldName)) {
                java.util.Map $36 = decodeMapStringList37(reader, wrapper, fieldPath);
                entity.mapListField = $36;
                continue;
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
