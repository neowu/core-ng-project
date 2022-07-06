public class EntityDecoder$TestEntity implements core.framework.mongo.impl.EntityDecoder {
    private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(core.framework.mongo.impl.EntityDecoder.class);

    private final core.framework.mongo.impl.EnumCodec enumCodecTestEnum20 = new core.framework.mongo.impl.EnumCodec(core.framework.mongo.impl.TestEnum.class);

    private java.util.List decodeListString11(org.bson.BsonReader reader, core.framework.mongo.impl.BsonReaderWrapper wrapper, String fieldPath) {
        java.util.List list = wrapper.startReadList(fieldPath);
        if (list == null) return null;
        reader.readStartArray();
        while (reader.readBsonType() != org.bson.BsonType.END_OF_DOCUMENT) {
            java.lang.String $12 = wrapper.readString(fieldPath);
            list.add($12);
        }
        reader.readEndArray();
        return list;
    }

    private java.util.Map decodeMapStringString14(org.bson.BsonReader reader, core.framework.mongo.impl.BsonReaderWrapper wrapper, String parentField) {
        java.util.Map map = wrapper.startReadMap(parentField);
        if (map == null) return null;
        reader.readStartDocument();
        while (reader.readBsonType() != org.bson.BsonType.END_OF_DOCUMENT) {
            String fieldName = reader.readName();
            String fieldPath = parentField + "." + fieldName;
            java.lang.String $15 = wrapper.readString(fieldPath);
            map.put(fieldName, $15);
        }
        reader.readEndDocument();
        return map;
    }

    private java.util.List decodeListTestEnum22(org.bson.BsonReader reader, core.framework.mongo.impl.BsonReaderWrapper wrapper, String fieldPath) {
        java.util.List list = wrapper.startReadList(fieldPath);
        if (list == null) return null;
        reader.readStartArray();
        while (reader.readBsonType() != org.bson.BsonType.END_OF_DOCUMENT) {
            core.framework.mongo.impl.TestEnum $23 = (core.framework.mongo.impl.TestEnum) enumCodecTestEnum20.read(reader, fieldPath);
            list.add($23);
        }
        reader.readEndArray();
        return list;
    }

    public core.framework.mongo.impl.TestChildEntity decodeTestChildEntity17(org.bson.BsonReader reader, core.framework.mongo.impl.BsonReaderWrapper wrapper, String parentField) {
        boolean hasContent = wrapper.startReadEntity(parentField);
        if (!hasContent) return null;
        core.framework.mongo.impl.TestChildEntity entity = new core.framework.mongo.impl.TestChildEntity();
        reader.readStartDocument();
        while (reader.readBsonType() != org.bson.BsonType.END_OF_DOCUMENT) {
            String fieldName = reader.readName();
            String fieldPath = parentField + "." + fieldName;
            if ("boolean_field".equals(fieldName)) {
                java.lang.Boolean $18 = wrapper.readBoolean(fieldPath);
                entity.booleanField = $18;
                continue;
            }
            if ("enum_field".equals(fieldName)) {
                core.framework.mongo.impl.TestEnum $19 = (core.framework.mongo.impl.TestEnum) enumCodecTestEnum20.read(reader, fieldPath);
                entity.enumField = $19;
                continue;
            }
            if ("enum_list_field".equals(fieldName)) {
                java.util.List $21 = decodeListTestEnum22(reader, wrapper, fieldPath);
                entity.enumListField = $21;
                continue;
            }
            if ("ref_id_field".equals(fieldName)) {
                org.bson.types.ObjectId $24 = wrapper.readObjectId(fieldPath);
                entity.refId = $24;
                continue;
            }
            logger.warn("undefined field, field={}, type={}", fieldPath, reader.getCurrentBsonType());
            reader.skipValue();
        }
        reader.readEndDocument();
        return entity;
    }

    private java.util.List decodeListTestChildEntity26(org.bson.BsonReader reader, core.framework.mongo.impl.BsonReaderWrapper wrapper, String fieldPath) {
        java.util.List list = wrapper.startReadList(fieldPath);
        if (list == null) return null;
        reader.readStartArray();
        while (reader.readBsonType() != org.bson.BsonType.END_OF_DOCUMENT) {
            core.framework.mongo.impl.TestChildEntity $27 = decodeTestChildEntity17(reader, wrapper, fieldPath);
            list.add($27);
        }
        reader.readEndArray();
        return list;
    }

    private java.util.Map decodeMapStringTestChildEntity29(org.bson.BsonReader reader, core.framework.mongo.impl.BsonReaderWrapper wrapper, String parentField) {
        java.util.Map map = wrapper.startReadMap(parentField);
        if (map == null) return null;
        reader.readStartDocument();
        while (reader.readBsonType() != org.bson.BsonType.END_OF_DOCUMENT) {
            String fieldName = reader.readName();
            String fieldPath = parentField + "." + fieldName;
            core.framework.mongo.impl.TestChildEntity $30 = decodeTestChildEntity17(reader, wrapper, fieldPath);
            map.put(fieldName, $30);
        }
        reader.readEndDocument();
        return map;
    }

    private java.util.Map decodeMapTestEnumString33(org.bson.BsonReader reader, core.framework.mongo.impl.BsonReaderWrapper wrapper, String parentField) {
        java.util.Map map = wrapper.startReadMap(parentField);
        if (map == null) return null;
        reader.readStartDocument();
        while (reader.readBsonType() != org.bson.BsonType.END_OF_DOCUMENT) {
            String fieldName = reader.readName();
            String fieldPath = parentField + "." + fieldName;
            java.lang.String $34 = wrapper.readString(fieldPath);
            map.put(enumCodecTestEnum20.decodeMapKey(fieldName), $34);
        }
        reader.readEndDocument();
        return map;
    }

    private java.util.Map decodeMapStringList36(org.bson.BsonReader reader, core.framework.mongo.impl.BsonReaderWrapper wrapper, String parentField) {
        java.util.Map map = wrapper.startReadMap(parentField);
        if (map == null) return null;
        reader.readStartDocument();
        while (reader.readBsonType() != org.bson.BsonType.END_OF_DOCUMENT) {
            String fieldName = reader.readName();
            String fieldPath = parentField + "." + fieldName;
            java.util.List $37 = decodeListString11(reader, wrapper, fieldPath);
            map.put(fieldName, $37);
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
            if ("date_field".equals(fieldName)) {
                java.time.LocalDateTime $7 = wrapper.readLocalDateTime(fieldPath);
                entity.dateField = $7;
                continue;
            }
            if ("zoned_date_time_field".equals(fieldName)) {
                java.time.ZonedDateTime $8 = wrapper.readZonedDateTime(fieldPath);
                entity.zonedDateTimeField = $8;
                continue;
            }
            if ("string_field".equals(fieldName)) {
                java.lang.String $9 = wrapper.readString(fieldPath);
                entity.stringField = $9;
                continue;
            }
            if ("list_field".equals(fieldName)) {
                java.util.List $10 = decodeListString11(reader, wrapper, fieldPath);
                entity.listField = $10;
                continue;
            }
            if ("map_field".equals(fieldName)) {
                java.util.Map $13 = decodeMapStringString14(reader, wrapper, fieldPath);
                entity.mapField = $13;
                continue;
            }
            if ("child".equals(fieldName)) {
                core.framework.mongo.impl.TestChildEntity $16 = decodeTestChildEntity17(reader, wrapper, fieldPath);
                entity.child = $16;
                continue;
            }
            if ("children".equals(fieldName)) {
                java.util.List $25 = decodeListTestChildEntity26(reader, wrapper, fieldPath);
                entity.children = $25;
                continue;
            }
            if ("children_map".equals(fieldName)) {
                java.util.Map $28 = decodeMapStringTestChildEntity29(reader, wrapper, fieldPath);
                entity.childrenMap = $28;
                continue;
            }
            if ("null_child".equals(fieldName)) {
                core.framework.mongo.impl.TestChildEntity $31 = decodeTestChildEntity17(reader, wrapper, fieldPath);
                entity.nullChild = $31;
                continue;
            }
            if ("enum_map_field".equals(fieldName)) {
                java.util.Map $32 = decodeMapTestEnumString33(reader, wrapper, fieldPath);
                entity.enumMapField = $32;
                continue;
            }
            if ("map_list_field".equals(fieldName)) {
                java.util.Map $35 = decodeMapStringList36(reader, wrapper, fieldPath);
                entity.mapListField = $35;
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
