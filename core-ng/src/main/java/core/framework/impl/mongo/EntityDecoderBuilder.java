package core.framework.impl.mongo;

import core.framework.api.mongo.Id;
import core.framework.api.util.Maps;
import core.framework.api.util.Types;
import core.framework.impl.asm.CodeBuilder;
import core.framework.impl.asm.DynamicInstanceBuilder;
import core.framework.impl.reflect.Classes;
import core.framework.impl.reflect.GenericTypes;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Map;

import static core.framework.impl.asm.Literal.type;
import static core.framework.impl.asm.Literal.variable;

/**
 * @author neo
 */
final class EntityDecoderBuilder<T> {
    final DynamicInstanceBuilder<EntityDecoder<T>> builder;
    private final Class<T> entityClass;
    private final Map<Class<? extends Enum<?>>, String> enumCodecFields = Maps.newHashMap();
    private final Map<Type, String> decodeMethods = Maps.newHashMap();
    private final String helper = EntityCodecHelper.class.getCanonicalName();
    private int index;

    EntityDecoderBuilder(Class<T> entityClass) {
        this.entityClass = entityClass;
        builder = new DynamicInstanceBuilder<>(EntityDecoder.class, EntityDecoder.class.getCanonicalName() + "$" + entityClass.getSimpleName());
    }

    public EntityDecoder<T> build() {
        builder.addField("private final {} logger = {}.getLogger({});", type(Logger.class), type(LoggerFactory.class), variable(EntityDecoder.class));
        String methodName = decodeEntityMethod(entityClass);
        CodeBuilder builder = new CodeBuilder()
                .append("public Object decode(org.bson.BsonReader reader) {\n")
                .indent(1).append("return {}(reader, {});\n", methodName, variable(""))
                .append("}");
        this.builder.addMethod(builder.build());
        return this.builder.build();
    }

    private String decodeEntityMethod(Class<?> entityClass) {
        String methodName = decodeMethods.get(entityClass);
        if (methodName != null) return methodName;

        methodName = "decode" + entityClass.getSimpleName() + (index++);
        CodeBuilder builder = new CodeBuilder().append("public {} {}(org.bson.BsonReader reader, String parentField) {\n", type(entityClass), methodName);
        builder.indent(1).append("org.bson.BsonType currentType = reader.getCurrentBsonType();\n");

        builder.indent(1).append("if (currentType != null && currentType == org.bson.BsonType.NULL) {\n");
        builder.indent(2).append("reader.readNull();\n");
        builder.indent(2).append("return null;\n");
        builder.indent(1).append("}\n");

        builder.indent(1).append("if (currentType != null && currentType != org.bson.BsonType.DOCUMENT) {\n");
        builder.indent(2).append("logger.warn({}, parentField, currentType);\n", variable("unexpected field type, field={}, type={}"));
        builder.indent(2).append("reader.skipValue();\n");
        builder.indent(2).append("return null;\n");
        builder.indent(1).append("}\n");

        builder.indent(1).append("{} entity = new {}();\n", type(entityClass), type(entityClass));

        builder.indent(1).append("reader.readStartDocument();\n")
               .indent(1).append("while (reader.readBsonType() != org.bson.BsonType.END_OF_DOCUMENT) {\n")
               .indent(2).append("String fieldName = reader.readName();\n")
               .indent(2).append("String fieldPath = parentField + \".\" + fieldName;\n");

        for (Field field : Classes.instanceFields(entityClass)) {
            decodeEntityField(builder, field);
        }

        builder.indent(2).append("logger.warn({}, fieldPath, reader.getCurrentBsonType());\n", variable("undefined field, field={}, type={}"));
        builder.indent(2).append("reader.skipValue();\n");
        builder.indent(1).append("}\n");

        builder.indent(1).append("reader.readEndDocument();\n");
        builder.indent(1).append("return entity;\n");
        builder.append('}');
        this.builder.addMethod(builder.build());

        decodeMethods.put(entityClass, methodName);
        return methodName;
    }

    private void decodeEntityField(CodeBuilder builder, Field field) {
        String fieldVariable = "entity." + field.getName();
        Class<?> fieldClass = field.getType();
        Type fieldType = field.getGenericType();

        String mongoFieldName;
        if (field.isAnnotationPresent(Id.class)) mongoFieldName = "_id";
        else mongoFieldName = field.getDeclaredAnnotation(core.framework.api.mongo.Field.class).name();

        builder.indent(2).append("if (\"{}\".equals(fieldName)) {\n", mongoFieldName);

        if (Integer.class.equals(fieldClass)) {
            builder.indent(3).append("{} = {}.readInteger(reader, fieldPath);\n", fieldVariable, helper);
        } else if (String.class.equals(fieldClass)) {
            builder.indent(3).append("{} = {}.readString(reader, fieldPath);\n", fieldVariable, helper);
        } else if (Long.class.equals(fieldClass)) {
            builder.indent(3).append("{} = {}.readLong(reader, fieldPath);\n", fieldVariable, helper);
        } else if (LocalDateTime.class.equals(fieldClass)) {
            builder.indent(3).append("{} = {}.readLocalDateTime(reader, fieldPath);\n", fieldVariable, helper);
        } else if (ZonedDateTime.class.equals(fieldClass)) {
            builder.indent(3).append("{} = {}.readZonedDateTime(reader, fieldPath);\n", fieldVariable, helper);
        } else if (fieldClass.isEnum()) {
            String enumCodecVariable = registerEnumCodec(fieldClass);
            builder.indent(3).append("{} = ({}) {}.read(reader, fieldPath);\n", fieldVariable, type(fieldClass), enumCodecVariable);
        } else if (Double.class.equals(fieldClass)) {
            builder.indent(3).append("{} = {}.readDouble(reader, fieldPath);\n", fieldVariable, helper);
        } else if (ObjectId.class.equals(fieldClass)) {
            builder.indent(3).append("{} = {}.readObjectId(reader, fieldPath);\n", fieldVariable, helper);
        } else if (Boolean.class.equals(fieldClass)) {
            builder.indent(3).append("{} = {}.readBoolean(reader, fieldPath);\n", fieldVariable, helper);
        } else if (GenericTypes.isGenericList(fieldType)) {
            String method = decodeListMethod(GenericTypes.listValueClass(fieldType));
            builder.indent(3).append("{} = {}(reader, fieldPath);\n", fieldVariable, method);
        } else if (GenericTypes.isGenericStringMap(fieldType)) {
            String method = decodeMapMethod(GenericTypes.mapValueClass(fieldType));
            builder.indent(3).append("{} = {}(reader, fieldPath);\n", fieldVariable, method);
        } else {
            String method = decodeEntityMethod(fieldClass);
            builder.indent(3).append("{} = {}(reader, fieldPath);\n", fieldVariable, method);
        }

        builder.indent(3).append("continue;\n");
        builder.indent(2).append("}\n");
    }

    private String decodeMapMethod(Class<?> valueClass) {
        String methodName = decodeMethods.get(Types.map(String.class, valueClass));
        if (methodName != null) return methodName;

        methodName = "decodeMap" + valueClass.getSimpleName() + (index++);
        CodeBuilder builder = new CodeBuilder();
        builder.append("private java.util.Map {}(org.bson.BsonReader reader, String parentField) {\n", methodName);
        builder.indent(1).append("org.bson.BsonType currentType = reader.getCurrentBsonType();\n");
        builder.indent(1).append("if (currentType == org.bson.BsonType.NULL) {\n        reader.readNull();\n        return null;\n    }\n");
        builder.indent(1).append("if (currentType != org.bson.BsonType.DOCUMENT) {\n");
        builder.indent(2).append("logger.warn({}, parentField, currentType);\n", variable("unexpected field type, field={}, type={}"));
        builder.indent(2).append("reader.skipValue();\n");
        builder.indent(2).append("return null;\n");
        builder.indent(1).append("}\n");

        builder.indent(1).append("java.util.Map map = new java.util.LinkedHashMap();\n");
        builder.indent(1).append("reader.readStartDocument();\n");
        builder.indent(1).append("while (reader.readBsonType() != org.bson.BsonType.END_OF_DOCUMENT) {\n");
        builder.indent(2).append("String fieldName = reader.readName();\n");
        builder.indent(2).append("String fieldPath = parentField + \".\" + fieldName;\n");
        if (Integer.class.equals(valueClass)) {
            builder.indent(2).append("map.put(fieldName, {}.readInteger(reader, fieldPath));\n", helper);
        } else if (String.class.equals(valueClass)) {
            builder.indent(2).append("map.put(fieldName, {}.readString(reader, fieldPath));\n", helper);
        } else if (Long.class.equals(valueClass)) {
            builder.indent(2).append("map.put(fieldName, {}.readLong(reader, fieldPath));\n", helper);
        } else if (LocalDateTime.class.equals(valueClass)) {
            builder.indent(2).append("map.put(fieldName, {}.readLocalDateTime(reader, fieldPath));\n", helper);
        } else if (ZonedDateTime.class.equals(valueClass)) {
            builder.indent(2).append("map.put(fieldName, {}.readZonedDateTime(reader, fieldPath));\n", helper);
        } else if (valueClass.isEnum()) {
            String enumCodecVariable = registerEnumCodec(valueClass);
            builder.indent(2).append("map.put(fieldName, {}.read(reader, fieldPath));\n", enumCodecVariable);
        } else if (Double.class.equals(valueClass)) {
            builder.indent(2).append("map.put(fieldName, {}.readDouble(reader, fieldPath));\n", helper);
        } else if (ObjectId.class.equals(valueClass)) {
            builder.indent(2).append("map.put(fieldName, {}.readObjectId(reader, fieldPath));\n", helper);
        } else if (Boolean.class.equals(valueClass)) {
            builder.indent(2).append("map.put(fieldName, {}.readBoolean(reader, fieldPath));\n", helper);
        } else {
            String method = decodeEntityMethod(valueClass);
            builder.indent(2).append("map.put(fieldName, {}(reader, fieldPath));\n", method);
        }
        builder.indent(1).append("}\n");
        builder.indent(1).append("reader.readEndDocument();\n");
        builder.indent(1).append("return map;\n");
        builder.append('}');
        this.builder.addMethod(builder.build());

        decodeMethods.put(Types.map(String.class, valueClass), methodName);
        return methodName;
    }

    private String decodeListMethod(Class<?> valueClass) {
        String methodName = decodeMethods.get(Types.list(valueClass));
        if (methodName != null) return methodName;

        methodName = "decodeList" + valueClass.getSimpleName() + (index++);
        CodeBuilder builder = new CodeBuilder();
        builder.append("private java.util.List {}(org.bson.BsonReader reader, String fieldPath) {\n", methodName);
        builder.indent(1).append("org.bson.BsonType currentType = reader.getCurrentBsonType();\n");
        builder.indent(1).append("if (currentType == org.bson.BsonType.NULL) {\n")
               .indent(2).append("reader.readNull();\n")
               .indent(2).append("return null;\n")
               .indent(1).append("}\n");
        builder.indent(1).append("if (currentType != org.bson.BsonType.ARRAY) {\n")
               .indent(2).append("logger.warn({}, fieldPath, currentType);\n", variable("unexpected field type, field={}, type={}"))
               .indent(2).append("reader.skipValue();\n")
               .indent(2).append("return null;\n")
               .indent(1).append("}\n");
        builder.indent(1).append("java.util.List list = new java.util.ArrayList();\n");
        builder.indent(1).append("reader.readStartArray();\n");
        builder.indent(1).append("while (reader.readBsonType() != org.bson.BsonType.END_OF_DOCUMENT) {\n");
        if (Integer.class.equals(valueClass)) {
            builder.indent(2).append("list.add({}.readInteger(reader, fieldPath));\n", helper);
        } else if (String.class.equals(valueClass)) {
            builder.indent(2).append("list.add({}.readString(reader, fieldPath));\n", helper);
        } else if (Long.class.equals(valueClass)) {
            builder.indent(2).append("list.add({}.readLong(reader, fieldPath));\n", helper);
        } else if (LocalDateTime.class.equals(valueClass)) {
            builder.indent(2).append("list.add({}.readLocalDateTime(reader, fieldPath));\n", helper);
        } else if (ZonedDateTime.class.equals(valueClass)) {
            builder.indent(2).append("list.add({}.readZonedDateTime(reader, fieldPath));\n", helper);
        } else if (valueClass.isEnum()) {
            String enumCodecVariable = registerEnumCodec(valueClass);
            builder.indent(2).append("list.add({}.read(reader, fieldPath));\n", enumCodecVariable, type(valueClass));
        } else if (Double.class.equals(valueClass)) {
            builder.indent(2).append("list.add({}.readDouble(reader, fieldPath));\n", helper);
        } else if (ObjectId.class.equals(valueClass)) {
            builder.indent(2).append("list.add({}.readObjectId(reader, fieldPath));\n", helper);
        } else if (Boolean.class.equals(valueClass)) {
            builder.indent(2).append("list.add({}.readBoolean(reader, fieldPath));\n", helper);
        } else {
            String method = decodeEntityMethod(valueClass);
            builder.indent(2).append("list.add({}(reader, fieldPath));\n", method);
        }
        builder.indent(1).append("}\n");
        builder.indent(1).append("reader.readEndArray();\n");
        builder.indent(1).append("return list;\n");
        builder.append('}');
        this.builder.addMethod(builder.build());

        decodeMethods.put(Types.list(valueClass), methodName);
        return methodName;
    }

    private String registerEnumCodec(Class<?> fieldClass) {
        @SuppressWarnings("unchecked")
        Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>) fieldClass;
        return enumCodecFields.computeIfAbsent(enumClass, key -> {
            String fieldVariable = "enumCodec" + fieldClass.getSimpleName() + (index++);
            builder.addField("private final {} {} = new {}({});", type(EnumCodec.class), fieldVariable, type(EnumCodec.class), variable(fieldClass));
            return fieldVariable;
        });
    }
}
