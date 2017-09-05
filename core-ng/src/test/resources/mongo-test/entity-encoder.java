public class EntityEncoder$TestEntity implements core.framework.impl.mongo.EntityEncoder {
    private final core.framework.impl.mongo.EnumCodec enumCodecTestEnum4 = new core.framework.impl.mongo.EnumCodec(core.framework.impl.mongo.TestEntityChild.TestEnum.class);

    private void encodeListString1(org.bson.BsonWriter writer, core.framework.impl.mongo.BsonWriterWrapper wrapper, java.util.List list) {
        writer.writeStartArray();
        for (java.util.Iterator iterator = list.iterator(); iterator.hasNext(); ) {
            java.lang.String value = (java.lang.String) iterator.next();
            wrapper.write(value);
        }
        writer.writeEndArray();
    }

    private void encodeMapString2(org.bson.BsonWriter writer, core.framework.impl.mongo.BsonWriterWrapper wrapper, java.util.Map map) {
        writer.writeStartDocument();
        for (java.util.Iterator iterator = map.entrySet().iterator(); iterator.hasNext(); ) {
            java.util.Map.Entry entry = (java.util.Map.Entry) iterator.next();
            String key = (String) entry.getKey();
            java.lang.String value = (java.lang.String) entry.getValue();
            writer.writeName(key);
            wrapper.write(value);
        }
        writer.writeEndDocument();
    }

    private void encodeListTestEnum5(org.bson.BsonWriter writer, core.framework.impl.mongo.BsonWriterWrapper wrapper, java.util.List list) {
        writer.writeStartArray();
        for (java.util.Iterator iterator = list.iterator(); iterator.hasNext(); ) {
            core.framework.impl.mongo.TestEntityChild.TestEnum value = (core.framework.impl.mongo.TestEntityChild.TestEnum) iterator.next();
            enumCodecTestEnum4.encode(writer, value, null);
        }
        writer.writeEndArray();
    }

    private void encodeTestEntityChild3(org.bson.BsonWriter writer, core.framework.impl.mongo.BsonWriterWrapper wrapper, core.framework.impl.mongo.TestEntityChild entity) {
        writer.writeStartDocument();
        writer.writeName("boolean_field");
        wrapper.write(entity.booleanField);
        writer.writeName("enum_field");
        enumCodecTestEnum4.encode(writer, entity.enumField, null);
        writer.writeName("enum_list_field");
        if (entity.enumListField == null) writer.writeNull();
        else encodeListTestEnum5(writer, wrapper, entity.enumListField);
        writer.writeName("ref_id_field");
        wrapper.write(entity.refId);
        writer.writeEndDocument();
    }

    private void encodeListTestEntityChild6(org.bson.BsonWriter writer, core.framework.impl.mongo.BsonWriterWrapper wrapper, java.util.List list) {
        writer.writeStartArray();
        for (java.util.Iterator iterator = list.iterator(); iterator.hasNext(); ) {
            core.framework.impl.mongo.TestEntityChild value = (core.framework.impl.mongo.TestEntityChild) iterator.next();
            if (value == null) writer.writeNull();
            else encodeTestEntityChild3(writer, wrapper, value);
        }
        writer.writeEndArray();
    }

    private void encodeMapTestEntityChild7(org.bson.BsonWriter writer, core.framework.impl.mongo.BsonWriterWrapper wrapper, java.util.Map map) {
        writer.writeStartDocument();
        for (java.util.Iterator iterator = map.entrySet().iterator(); iterator.hasNext(); ) {
            java.util.Map.Entry entry = (java.util.Map.Entry) iterator.next();
            String key = (String) entry.getKey();
            core.framework.impl.mongo.TestEntityChild value = (core.framework.impl.mongo.TestEntityChild) entry.getValue();
            writer.writeName(key);
            if (value == null) writer.writeNull();
            else encodeTestEntityChild3(writer, wrapper, value);
        }
        writer.writeEndDocument();
    }

    private void encodeTestEntity0(org.bson.BsonWriter writer, core.framework.impl.mongo.BsonWriterWrapper wrapper, core.framework.impl.mongo.TestEntity entity) {
        writer.writeStartDocument();
        writer.writeName("_id");
        wrapper.write(entity.id);
        writer.writeName("int_field");
        wrapper.write(entity.intField);
        writer.writeName("double_field");
        wrapper.write(entity.doubleField);
        writer.writeName("long_field");
        wrapper.write(entity.longField);
        writer.writeName("boolean_field");
        wrapper.write(entity.booleanField);
        writer.writeName("date_field");
        wrapper.write(entity.dateField);
        writer.writeName("zoned_date_time_field");
        wrapper.write(entity.zonedDateTimeField);
        writer.writeName("string_field");
        wrapper.write(entity.stringField);
        writer.writeName("list_field");
        if (entity.listField == null) writer.writeNull();
        else encodeListString1(writer, wrapper, entity.listField);
        writer.writeName("map_field");
        if (entity.mapField == null) writer.writeNull();
        else encodeMapString2(writer, wrapper, entity.mapField);
        writer.writeName("child");
        if (entity.child == null) writer.writeNull();
        else encodeTestEntityChild3(writer, wrapper, entity.child);
        writer.writeName("children");
        if (entity.children == null) writer.writeNull();
        else encodeListTestEntityChild6(writer, wrapper, entity.children);
        writer.writeName("children_map");
        if (entity.childrenMap == null) writer.writeNull();
        else encodeMapTestEntityChild7(writer, wrapper, entity.childrenMap);
        writer.writeName("null_child");
        if (entity.nullChild == null) writer.writeNull();
        else encodeTestEntityChild3(writer, wrapper, entity.nullChild);
        writer.writeEndDocument();
    }

    public void encode(org.bson.BsonWriter writer, Object entity) {
        core.framework.impl.mongo.BsonWriterWrapper wrapper = new core.framework.impl.mongo.BsonWriterWrapper(writer);
        encodeTestEntity0(writer, wrapper, (core.framework.impl.mongo.TestEntity) entity);
    }

}
