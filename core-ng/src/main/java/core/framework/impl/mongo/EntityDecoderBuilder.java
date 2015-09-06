package core.framework.impl.mongo;

import core.framework.api.mongo.Id;
import core.framework.impl.code.CodeBuilder;
import core.framework.impl.code.DynamicInstanceBuilder;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public class EntityDecoderBuilder<T> {
    private final Class<T> entityClass;
    private final String helper = EntityCodecHelper.class.getCanonicalName();

    final Map<String, String> methods = new LinkedHashMap<>();

    public EntityDecoderBuilder(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public EntityDecoder<T> build() {
        DynamicInstanceBuilder<EntityDecoder<T>> builder = new DynamicInstanceBuilder<>(EntityDecoder.class, EntityDecoder.class.getCanonicalName() + "$" + entityClass.getSimpleName());
        builder.addField(loggerField());
        buildMethods();
        methods.values().forEach(builder::addMethod);
        return builder.build();
    }

    private String loggerField() {
        return "private final " + Logger.class.getCanonicalName() + " logger = " + LoggerFactory.class.getCanonicalName() + ".getLogger(" + EntityDecoder.class.getCanonicalName() + ".class);";
    }

    private void buildMethods() {
        String methodName = decodeEntityMethod(entityClass);
        CodeBuilder builder = new CodeBuilder().append("public Object decode(org.bson.BsonReader reader) {\n")
            .indent(1).append("return {}(reader, \"\");\n", methodName)
            .append("}");

        methods.put("decode", builder.build());
    }

    private String decodeEntityMethod(Class entityClass) {
        String entityClassName = entityClass.getCanonicalName();
        String methodName = "decode_" + entityClassName.replaceAll("\\.", "_");
        if (methods.containsKey(methodName)) return methodName;

        CodeBuilder builder = new CodeBuilder().append("public {} {}(org.bson.BsonReader reader, String parentField) {\n", entityClassName, methodName);
        builder.indent(1).append("org.bson.BsonType currentType = reader.getCurrentBsonType();\n");

        builder.indent(1).append("if (currentType != null && currentType == org.bson.BsonType.NULL) {\n");
        builder.indent(2).append("reader.readNull();\n");
        builder.indent(2).append("return null;\n");
        builder.indent(1).append("}\n");

        builder.indent(1).append("if (currentType != null && currentType != org.bson.BsonType.DOCUMENT) {\n");
        builder.indent(2).append("logger.warn(\"field returned from mongo is ignored, field={}\", parentField);\n");
        builder.indent(2).append("reader.skipValue();\n");
        builder.indent(2).append("return null;\n");
        builder.indent(1).append("}\n");

        builder.indent(1).append("{} entity = new {}();\n", entityClassName, entityClassName);

        builder.indent(1).append("reader.readStartDocument();\n")
            .indent(1).append("while (reader.readBsonType() != org.bson.BsonType.END_OF_DOCUMENT) {\n")
            .indent(2).append("String fieldName = reader.readName();\n")
            .indent(2).append("String fieldPath = parentField + \".\" + fieldName;\n")
            .indent(2).append("currentType = reader.getCurrentBsonType();\n");

        for (Field field : entityClass.getFields()) {
            decodeEntityField(builder, field);
        }

        builder.indent(2).append("logger.warn(\"field returned from mongo is ignored, field={}\", fieldPath);\n");
        builder.indent(2).append("reader.skipValue();\n");
        builder.indent(1).append("}\n");

        builder.indent(1).append("reader.readEndDocument();\n");
        builder.indent(1).append("return entity;\n");
        builder.append("}\n");

        methods.put(methodName, builder.build());
        return methodName;
    }

    private void decodeEntityField(CodeBuilder builder, Field field) {
        String fieldVariable = "entity." + field.getName();
        if (field.isAnnotationPresent(Id.class)) {
            builder.indent(2).append("if (\"_id\".equals(fieldName) && currentType == org.bson.BsonType.OBJECT_ID) {\n", fieldVariable);
            builder.indent(3).append("{} = reader.readObjectId(); continue;\n", fieldVariable);
            builder.indent(2).append("}\n", fieldVariable);
        } else {
            Type fieldType = field.getGenericType();
            builder.indent(2).append("if (\"{}\".equals(fieldName)) {\n", field.getDeclaredAnnotation(core.framework.api.mongo.Field.class).name());
            Class fieldClass = (Class) (fieldType instanceof Class ? fieldType : ((ParameterizedType) fieldType).getRawType());

            if (Integer.class.equals(fieldClass)) {
                builder.indent(3).append("{} = {}.readInteger(reader, currentType, fieldPath);\n", fieldVariable, helper);
            } else if (String.class.equals(fieldClass)) {
                builder.indent(3).append("{} = {}.readString(reader, currentType, fieldPath);\n", fieldVariable, helper);
            } else if (Long.class.equals(fieldClass)) {
                builder.indent(3).append("{} = {}.readLong(reader, currentType, fieldPath);\n", fieldVariable, helper);
            } else if (LocalDateTime.class.equals(fieldClass)) {
                builder.indent(3).append("{} = {}.readLocalDateTime(reader, currentType, fieldPath);\n", fieldVariable, helper);
            } else if (Enum.class.isAssignableFrom(fieldClass)) {
                builder.indent(3).append("{} = ({}) {}.readEnum(reader, currentType, {}.class, fieldPath);\n", fieldVariable, fieldClass.getCanonicalName(), helper, fieldClass.getCanonicalName());
            } else if (Double.class.equals(fieldClass)) {
                builder.indent(3).append("{} = {}.readDouble(reader, currentType, fieldPath);\n", fieldVariable, helper);
            } else if (ObjectId.class.equals(fieldClass)) {
                builder.indent(3).append("{} = {}.readObjectId(reader, currentType, fieldPath);\n", fieldVariable, helper);
            } else if (Boolean.class.equals(fieldClass)) {
                builder.indent(3).append("{} = {}.readBoolean(reader, currentType, fieldPath);\n", fieldVariable, helper);
            } else if (List.class.equals(fieldClass) && fieldType instanceof ParameterizedType) {
                Class valueClass = (Class) ((ParameterizedType) fieldType).getActualTypeArguments()[0];
                String method = decodeListMethod(valueClass);
                builder.indent(3).append("{} = {}(reader, fieldPath);\n", fieldVariable, method);
            } else if (Map.class.equals(fieldClass) && fieldType instanceof ParameterizedType) {
                Class valueClass = (Class) ((ParameterizedType) fieldType).getActualTypeArguments()[1];
                String method = decodeMapMethod(valueClass);
                builder.indent(3).append("{} = {}(reader, fieldPath);\n", fieldVariable, method);
            } else {
                String method = decodeEntityMethod(fieldClass);
                builder.indent(3).append("{} = {}(reader, fieldPath);\n", fieldVariable, method);
            }

            builder.indent(3).append("continue;\n");
            builder.indent(2).append("}\n");
        }
    }

    private String decodeMapMethod(Class valueClass) {
        String valueClassName = valueClass.getCanonicalName();
        String methodName = ("decode_" + Map.class.getCanonicalName() + "_" + valueClassName).replaceAll("\\.", "_");
        if (methods.containsKey(methodName)) return methodName;

        CodeBuilder builder = new CodeBuilder();
        builder.append("private java.util.Map {}(org.bson.BsonReader reader, String parentField) {\n", methodName);
        builder.indent(1).append("org.bson.BsonType currentType = reader.getCurrentBsonType();\n");

        builder.indent(1).append("if (currentType == org.bson.BsonType.NULL) {\n");
        builder.indent(2).append("reader.readNull();\n");
        builder.indent(2).append("return null;\n");
        builder.indent(1).append("}\n");

        builder.indent(1).append("if (currentType != org.bson.BsonType.DOCUMENT) {\n");
        builder.indent(2).append("logger.warn(\"field returned from mongo is ignored, field={}\", parentField);\n");
        builder.indent(2).append("reader.skipValue();\n");
        builder.indent(2).append("return null;\n");
        builder.indent(1).append("}\n");


        builder.indent(1).append("java.util.Map map = new java.util.LinkedHashMap();\n");
        builder.indent(1).append("reader.readStartDocument();\n");
        builder.indent(1).append("while (reader.readBsonType() != org.bson.BsonType.END_OF_DOCUMENT) {\n");
        builder.indent(2).append("String fieldName = reader.readName();\n");
        builder.indent(2).append("String fieldPath = parentField + \".\" + fieldName;\n");
        builder.indent(2).append("currentType = reader.getCurrentBsonType();\n");

        if (Integer.class.equals(valueClass)) {
            builder.indent(2).append("map.put(fieldName, {}.readInteger(reader, currentType, fieldPath));\n", helper);
        } else if (String.class.equals(valueClass)) {
            builder.indent(2).append("map.put(fieldName, {}.readString(reader, currentType, fieldPath));\n", helper);
        } else if (Long.class.equals(valueClass)) {
            builder.indent(2).append("map.put(fieldName, {}.readLong(reader, currentType, fieldPath));\n", helper);
        } else if (LocalDateTime.class.equals(valueClass)) {
            builder.indent(2).append("map.put(fieldName, {}.readLocalDateTime(reader, currentType, fieldPath));\n", helper);
        } else if (Enum.class.isAssignableFrom(valueClass)) {
            builder.indent(2).append("map.put(fieldName, {}.readEnum(reader, currentType, {}.class, fieldPath));\n", helper, valueClassName);
        } else if (Double.class.equals(valueClass)) {
            builder.indent(2).append("map.put(fieldName, {}.readDouble(reader, currentType, fieldPath));\n", helper);
        } else if (ObjectId.class.equals(valueClass)) {
            builder.indent(2).append("map.put(fieldName, {}.readObjectId(reader, currentType, fieldPath));\n", helper);
        } else if (Boolean.class.equals(valueClass)) {
            builder.indent(2).append("map.put(fieldName, {}.readBoolean(reader, currentType, fieldPath));\n", helper);
        } else {
            String method = decodeEntityMethod(valueClass);
            builder.indent(2).append("map.put(fieldName, {}(reader, fieldPath));\n", method);
        }

        builder.indent(1).append("}\n");
        builder.indent(1).append("reader.readEndDocument();\n");

        builder.indent(1).append("return map;\n");
        builder.append("}\n");

        methods.put(methodName, builder.build());
        return methodName;
    }

    private String decodeListMethod(Class valueClass) {
        String valueClassName = valueClass.getCanonicalName();
        String methodName = ("decode_" + List.class.getCanonicalName() + "_" + valueClassName).replaceAll("\\.", "_");
        if (methods.containsKey(methodName)) return methodName;

        CodeBuilder builder = new CodeBuilder();
        builder.append("private java.util.List {}(org.bson.BsonReader reader, String fieldPath) {\n", methodName);
        builder.indent(1).append("org.bson.BsonType currentType = reader.getCurrentBsonType();\n");

        builder.indent(1).append("if (currentType == org.bson.BsonType.NULL) {\n");
        builder.indent(2).append("reader.readNull();\n");
        builder.indent(2).append("return null;\n");
        builder.indent(1).append("}\n");

        builder.indent(1).append("if (currentType != org.bson.BsonType.ARRAY) {\n");
        builder.indent(2).append("logger.warn(\"field returned from mongo is ignored, field={}\", fieldPath);\n");
        builder.indent(2).append("reader.skipValue();\n");
        builder.indent(2).append("return null;\n");
        builder.indent(1).append("}\n");

        builder.indent(1).append("java.util.List list = new java.util.ArrayList();\n");
        builder.indent(1).append("reader.readStartArray();\n");
        builder.indent(1).append("while (reader.readBsonType() != org.bson.BsonType.END_OF_DOCUMENT) {\n");
        builder.indent(2).append("currentType = reader.getCurrentBsonType();\n");

        if (Integer.class.equals(valueClass)) {
            builder.indent(2).append("list.add({}.readInteger(reader, currentType, fieldPath));\n", helper);
        } else if (String.class.equals(valueClass)) {
            builder.indent(2).append("list.add({}.readString(reader, currentType, fieldPath));\n", helper);
        } else if (Long.class.equals(valueClass)) {
            builder.indent(2).append("list.add({}.readLong(reader, currentType, fieldPath));\n", helper);
        } else if (LocalDateTime.class.equals(valueClass)) {
            builder.indent(2).append("list.add({}.readLocalDateTime(reader, currentType, fieldPath));\n", helper);
        } else if (Enum.class.isAssignableFrom(valueClass)) {
            builder.indent(2).append("list.add({}.readEnum(reader, currentType, {}.class, fieldPath));\n", helper, valueClassName);
        } else if (Double.class.equals(valueClass)) {
            builder.indent(2).append("list.add({}.readDouble(reader, currentType, fieldPath));\n", helper);
        } else if (ObjectId.class.equals(valueClass)) {
            builder.indent(2).append("list.add({}.readObjectId(reader, currentType, fieldPath));\n", helper);
        } else if (Boolean.class.equals(valueClass)) {
            builder.indent(2).append("list.add({}.readBoolean(reader, currentType, fieldPath));\n", helper);
        } else {
            String method = decodeEntityMethod(valueClass);
            builder.indent(2).append("list.add({}(reader, fieldPath));\n", method);
        }

        builder.indent(1).append("}\n");
        builder.indent(1).append("reader.readEndArray();\n");
        builder.indent(1).append("return list;\n");
        builder.append("}\n");

        methods.put(methodName, builder.build());
        return methodName;
    }
}
