public class EntityDecoder$TestEntity implements core.framework.impl.mongo.EntityDecoder {
    private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(core.framework.impl.mongo.EntityDecoder.class);

    private final core.framework.impl.mongo.EnumCodec enumCodecTestEnum4 = new core.framework.impl.mongo.EnumCodec(core.framework.impl.mongo.TestEntityChild.TestEnum.class);

    private java.util.List decodeListString1(org.bson.BsonReader reader, String fieldPath) {
        org.bson.BsonType currentType = reader.getCurrentBsonType();
        if (currentType == org.bson.BsonType.NULL) {
            reader.readNull();
            return null;
        }
        if (currentType != org.bson.BsonType.ARRAY) {
            logger.warn("unexpected field type, field={}, type={}", fieldPath, currentType);
            reader.skipValue();
            return null;
        }
        java.util.List list = new java.util.ArrayList();
        reader.readStartArray();
        while (reader.readBsonType() != org.bson.BsonType.END_OF_DOCUMENT) {
            list.add(core.framework.impl.mongo.EntityCodecHelper.readString(reader, fieldPath));
        }
        reader.readEndArray();
        return list;
    }

    private java.util.Map decodeMapString2(org.bson.BsonReader reader, String parentField) {
        org.bson.BsonType currentType = reader.getCurrentBsonType();
        if (currentType == org.bson.BsonType.NULL) {
            reader.readNull();
            return null;
        }
        if (currentType != org.bson.BsonType.DOCUMENT) {
            logger.warn("unexpected field type, field={}, type={}", parentField, currentType);
            reader.skipValue();
            return null;
        }
        java.util.Map map = new java.util.LinkedHashMap();
        reader.readStartDocument();
        while (reader.readBsonType() != org.bson.BsonType.END_OF_DOCUMENT) {
            String fieldName = reader.readName();
            String fieldPath = parentField + "." + fieldName;
            map.put(fieldName, core.framework.impl.mongo.EntityCodecHelper.readString(reader, fieldPath));
        }
        reader.readEndDocument();
        return map;
    }

    private java.util.List decodeListTestEnum5(org.bson.BsonReader reader, String fieldPath) {
        org.bson.BsonType currentType = reader.getCurrentBsonType();
        if (currentType == org.bson.BsonType.NULL) {
            reader.readNull();
            return null;
        }
        if (currentType != org.bson.BsonType.ARRAY) {
            logger.warn("unexpected field type, field={}, type={}", fieldPath, currentType);
            reader.skipValue();
            return null;
        }
        java.util.List list = new java.util.ArrayList();
        reader.readStartArray();
        while (reader.readBsonType() != org.bson.BsonType.END_OF_DOCUMENT) {
            list.add(enumCodecTestEnum4.read(reader, fieldPath));
        }
        reader.readEndArray();
        return list;
    }

    public core.framework.impl.mongo.TestEntityChild decodeTestEntityChild3(org.bson.BsonReader reader, String parentField) {
        org.bson.BsonType currentType = reader.getCurrentBsonType();
        if (currentType != null && currentType == org.bson.BsonType.NULL) {
            reader.readNull();
            return null;
        }
        if (currentType != null && currentType != org.bson.BsonType.DOCUMENT) {
            logger.warn("unexpected field type, field={}, type={}", parentField, currentType);
            reader.skipValue();
            return null;
        }
        core.framework.impl.mongo.TestEntityChild entity = new core.framework.impl.mongo.TestEntityChild();
        reader.readStartDocument();
        while (reader.readBsonType() != org.bson.BsonType.END_OF_DOCUMENT) {
            String fieldName = reader.readName();
            String fieldPath = parentField + "." + fieldName;
            if ("boolean_field".equals(fieldName)) {
                entity.booleanField = core.framework.impl.mongo.EntityCodecHelper.readBoolean(reader, fieldPath);
                continue;
            }
            if ("enum_field".equals(fieldName)) {
                entity.enumField = (core.framework.impl.mongo.TestEntityChild.TestEnum) enumCodecTestEnum4.decode(reader, null);
                continue;
            }
            if ("enum_list_field".equals(fieldName)) {
                entity.enumListField = decodeListTestEnum5(reader, fieldPath);
                continue;
            }
            if ("ref_id_field".equals(fieldName)) {
                entity.refId = core.framework.impl.mongo.EntityCodecHelper.readObjectId(reader, fieldPath);
                continue;
            }
            logger.warn("undefined field, field={}, type={}", fieldPath, reader.getCurrentBsonType());
            reader.skipValue();
        }
        reader.readEndDocument();
        return entity;
    }

    private java.util.List decodeListTestEntityChild6(org.bson.BsonReader reader, String fieldPath) {
        org.bson.BsonType currentType = reader.getCurrentBsonType();
        if (currentType == org.bson.BsonType.NULL) {
            reader.readNull();
            return null;
        }
        if (currentType != org.bson.BsonType.ARRAY) {
            logger.warn("unexpected field type, field={}, type={}", fieldPath, currentType);
            reader.skipValue();
            return null;
        }
        java.util.List list = new java.util.ArrayList();
        reader.readStartArray();
        while (reader.readBsonType() != org.bson.BsonType.END_OF_DOCUMENT) {
            list.add(decodeTestEntityChild3(reader, fieldPath));
        }
        reader.readEndArray();
        return list;
    }

    private java.util.Map decodeMapTestEntityChild7(org.bson.BsonReader reader, String parentField) {
        org.bson.BsonType currentType = reader.getCurrentBsonType();
        if (currentType == org.bson.BsonType.NULL) {
            reader.readNull();
            return null;
        }
        if (currentType != org.bson.BsonType.DOCUMENT) {
            logger.warn("unexpected field type, field={}, type={}", parentField, currentType);
            reader.skipValue();
            return null;
        }
        java.util.Map map = new java.util.LinkedHashMap();
        reader.readStartDocument();
        while (reader.readBsonType() != org.bson.BsonType.END_OF_DOCUMENT) {
            String fieldName = reader.readName();
            String fieldPath = parentField + "." + fieldName;
            map.put(fieldName, decodeTestEntityChild3(reader, fieldPath));
        }
        reader.readEndDocument();
        return map;
    }

    public core.framework.impl.mongo.TestEntity decodeTestEntity0(org.bson.BsonReader reader, String parentField) {
        org.bson.BsonType currentType = reader.getCurrentBsonType();
        if (currentType != null && currentType == org.bson.BsonType.NULL) {
            reader.readNull();
            return null;
        }
        if (currentType != null && currentType != org.bson.BsonType.DOCUMENT) {
            logger.warn("unexpected field type, field={}, type={}", parentField, currentType);
            reader.skipValue();
            return null;
        }
        core.framework.impl.mongo.TestEntity entity = new core.framework.impl.mongo.TestEntity();
        reader.readStartDocument();
        while (reader.readBsonType() != org.bson.BsonType.END_OF_DOCUMENT) {
            String fieldName = reader.readName();
            String fieldPath = parentField + "." + fieldName;
            if ("_id".equals(fieldName)) {
                entity.id = core.framework.impl.mongo.EntityCodecHelper.readObjectId(reader, fieldPath);
                continue;
            }
            if ("int_field".equals(fieldName)) {
                entity.intField = core.framework.impl.mongo.EntityCodecHelper.readInteger(reader, fieldPath);
                continue;
            }
            if ("double_field".equals(fieldName)) {
                entity.doubleField = core.framework.impl.mongo.EntityCodecHelper.readDouble(reader, fieldPath);
                continue;
            }
            if ("long_field".equals(fieldName)) {
                entity.longField = core.framework.impl.mongo.EntityCodecHelper.readLong(reader, fieldPath);
                continue;
            }
            if ("boolean_field".equals(fieldName)) {
                entity.booleanField = core.framework.impl.mongo.EntityCodecHelper.readBoolean(reader, fieldPath);
                continue;
            }
            if ("date_field".equals(fieldName)) {
                entity.dateField = core.framework.impl.mongo.EntityCodecHelper.readLocalDateTime(reader, fieldPath);
                continue;
            }
            if ("zoned_date_time_field".equals(fieldName)) {
                entity.zonedDateTimeField = core.framework.impl.mongo.EntityCodecHelper.readZonedDateTime(reader, fieldPath);
                continue;
            }
            if ("string_field".equals(fieldName)) {
                entity.stringField = core.framework.impl.mongo.EntityCodecHelper.readString(reader, fieldPath);
                continue;
            }
            if ("list_field".equals(fieldName)) {
                entity.listField = decodeListString1(reader, fieldPath);
                continue;
            }
            if ("map_field".equals(fieldName)) {
                entity.mapField = decodeMapString2(reader, fieldPath);
                continue;
            }
            if ("child".equals(fieldName)) {
                entity.child = decodeTestEntityChild3(reader, fieldPath);
                continue;
            }
            if ("children".equals(fieldName)) {
                entity.children = decodeListTestEntityChild6(reader, fieldPath);
                continue;
            }
            if ("children_map".equals(fieldName)) {
                entity.childrenMap = decodeMapTestEntityChild7(reader, fieldPath);
                continue;
            }
            if ("null_child".equals(fieldName)) {
                entity.nullChild = decodeTestEntityChild3(reader, fieldPath);
                continue;
            }
            logger.warn("undefined field, field={}, type={}", fieldPath, reader.getCurrentBsonType());
            reader.skipValue();
        }
        reader.readEndDocument();
        return entity;
    }

    public Object decode(org.bson.BsonReader reader) {
        return decodeTestEntity0(reader, "");
    }

}
