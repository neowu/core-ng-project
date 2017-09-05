public class EntityDecoder$TestEntity implements core.framework.impl.mongo.EntityDecoder {
    private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(core.framework.impl.mongo.EntityDecoder.class);

    private final core.framework.impl.mongo.EnumCodec enumCodecTestEnum19 = new core.framework.impl.mongo.EnumCodec(core.framework.impl.mongo.TestEntityChild.TestEnum.class);

    private java.util.List decodeListString10(org.bson.BsonReader reader, core.framework.impl.mongo.BsonReaderWrapper wrapper, String fieldPath) {
        java.util.List list = wrapper.startReadList(fieldPath);
        if (list == null) return null;
        reader.readStartArray();
        while (reader.readBsonType() != org.bson.BsonType.END_OF_DOCUMENT) {
            java.lang.String $11 = wrapper.readString(fieldPath);
            list.add($11);
        }
        reader.readEndArray();
        return list;
    }

    private java.util.Map decodeMapString13(org.bson.BsonReader reader, core.framework.impl.mongo.BsonReaderWrapper wrapper, String parentField) {
        java.util.Map map = wrapper.startReadMap(parentField);
        if (map == null) return null;
        reader.readStartDocument();
        while (reader.readBsonType() != org.bson.BsonType.END_OF_DOCUMENT) {
            String fieldName = reader.readName();
            String fieldPath = parentField + "." + fieldName;
            java.lang.String $14 = wrapper.readString(fieldPath);
            map.put(fieldName, $14);
        }
        reader.readEndDocument();
        return map;
    }

    private java.util.List decodeListTestEnum21(org.bson.BsonReader reader, core.framework.impl.mongo.BsonReaderWrapper wrapper, String fieldPath) {
        java.util.List list = wrapper.startReadList(fieldPath);
        if (list == null) return null;
        reader.readStartArray();
        while (reader.readBsonType() != org.bson.BsonType.END_OF_DOCUMENT) {
            core.framework.impl.mongo.TestEntityChild.TestEnum $22 = (core.framework.impl.mongo.TestEntityChild.TestEnum) enumCodecTestEnum19.read(reader, fieldPath);
            list.add($22);
        }
        reader.readEndArray();
        return list;
    }

    public core.framework.impl.mongo.TestEntityChild decodeTestEntityChild16(org.bson.BsonReader reader, core.framework.impl.mongo.BsonReaderWrapper wrapper, String parentField) {
        boolean hasContent = wrapper.startReadEntity(parentField);
        if (!hasContent) return null;
        core.framework.impl.mongo.TestEntityChild entity = new core.framework.impl.mongo.TestEntityChild();
        reader.readStartDocument();
        while (reader.readBsonType() != org.bson.BsonType.END_OF_DOCUMENT) {
            String fieldName = reader.readName();
            String fieldPath = parentField + "." + fieldName;
            if ("boolean_field".equals(fieldName)) {
                java.lang.Boolean $17 = wrapper.readBoolean(fieldPath);
                entity.booleanField = $17;
                continue;
            }
            if ("enum_field".equals(fieldName)) {
                core.framework.impl.mongo.TestEntityChild.TestEnum $18 = (core.framework.impl.mongo.TestEntityChild.TestEnum) enumCodecTestEnum19.read(reader, fieldPath);
                entity.enumField = $18;
                continue;
            }
            if ("enum_list_field".equals(fieldName)) {
                java.util.List $20 = decodeListTestEnum21(reader, wrapper, fieldPath);
                entity.enumListField = $20;
                continue;
            }
            if ("ref_id_field".equals(fieldName)) {
                org.bson.types.ObjectId $23 = wrapper.readObjectId(fieldPath);
                entity.refId = $23;
                continue;
            }
            logger.warn("undefined field, field={}, type={}", fieldPath, reader.getCurrentBsonType());
            reader.skipValue();
        }
        reader.readEndDocument();
        return entity;
    }

    private java.util.List decodeListTestEntityChild25(org.bson.BsonReader reader, core.framework.impl.mongo.BsonReaderWrapper wrapper, String fieldPath) {
        java.util.List list = wrapper.startReadList(fieldPath);
        if (list == null) return null;
        reader.readStartArray();
        while (reader.readBsonType() != org.bson.BsonType.END_OF_DOCUMENT) {
            core.framework.impl.mongo.TestEntityChild $26 = decodeTestEntityChild16(reader, wrapper, fieldPath);
            list.add($26);
        }
        reader.readEndArray();
        return list;
    }

    private java.util.Map decodeMapTestEntityChild28(org.bson.BsonReader reader, core.framework.impl.mongo.BsonReaderWrapper wrapper, String parentField) {
        java.util.Map map = wrapper.startReadMap(parentField);
        if (map == null) return null;
        reader.readStartDocument();
        while (reader.readBsonType() != org.bson.BsonType.END_OF_DOCUMENT) {
            String fieldName = reader.readName();
            String fieldPath = parentField + "." + fieldName;
            core.framework.impl.mongo.TestEntityChild $29 = decodeTestEntityChild16(reader, wrapper, fieldPath);
            map.put(fieldName, $29);
        }
        reader.readEndDocument();
        return map;
    }

    public core.framework.impl.mongo.TestEntity decodeTestEntity0(org.bson.BsonReader reader, core.framework.impl.mongo.BsonReaderWrapper wrapper, String parentField) {
        boolean hasContent = wrapper.startReadEntity(parentField);
        if (!hasContent) return null;
        core.framework.impl.mongo.TestEntity entity = new core.framework.impl.mongo.TestEntity();
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
            if ("long_field".equals(fieldName)) {
                java.lang.Long $4 = wrapper.readLong(fieldPath);
                entity.longField = $4;
                continue;
            }
            if ("boolean_field".equals(fieldName)) {
                java.lang.Boolean $5 = wrapper.readBoolean(fieldPath);
                entity.booleanField = $5;
                continue;
            }
            if ("date_field".equals(fieldName)) {
                java.time.LocalDateTime $6 = wrapper.readLocalDateTime(fieldPath);
                entity.dateField = $6;
                continue;
            }
            if ("zoned_date_time_field".equals(fieldName)) {
                java.time.ZonedDateTime $7 = wrapper.readZonedDateTime(fieldPath);
                entity.zonedDateTimeField = $7;
                continue;
            }
            if ("string_field".equals(fieldName)) {
                java.lang.String $8 = wrapper.readString(fieldPath);
                entity.stringField = $8;
                continue;
            }
            if ("list_field".equals(fieldName)) {
                java.util.List $9 = decodeListString10(reader, wrapper, fieldPath);
                entity.listField = $9;
                continue;
            }
            if ("map_field".equals(fieldName)) {
                java.util.Map $12 = decodeMapString13(reader, wrapper, fieldPath);
                entity.mapField = $12;
                continue;
            }
            if ("child".equals(fieldName)) {
                core.framework.impl.mongo.TestEntityChild $15 = decodeTestEntityChild16(reader, wrapper, fieldPath);
                entity.child = $15;
                continue;
            }
            if ("children".equals(fieldName)) {
                java.util.List $24 = decodeListTestEntityChild25(reader, wrapper, fieldPath);
                entity.children = $24;
                continue;
            }
            if ("children_map".equals(fieldName)) {
                java.util.Map $27 = decodeMapTestEntityChild28(reader, wrapper, fieldPath);
                entity.childrenMap = $27;
                continue;
            }
            if ("null_child".equals(fieldName)) {
                core.framework.impl.mongo.TestEntityChild $30 = decodeTestEntityChild16(reader, wrapper, fieldPath);
                entity.nullChild = $30;
                continue;
            }
            logger.warn("undefined field, field={}, type={}", fieldPath, reader.getCurrentBsonType());
            reader.skipValue();
        }
        reader.readEndDocument();
        return entity;
    }

    public Object decode(org.bson.BsonReader reader) {
        core.framework.impl.mongo.BsonReaderWrapper wrapper = new core.framework.impl.mongo.BsonReaderWrapper(reader);
        return decodeTestEntity0(reader, wrapper, "");
    }

}
