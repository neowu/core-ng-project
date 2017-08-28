package core.framework.impl.mongo;

import core.framework.api.mongo.Id;
import core.framework.api.util.Lists;
import core.framework.api.util.Sets;
import core.framework.api.util.Strings;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static core.framework.impl.asm.Literal.type;
import static core.framework.impl.asm.Literal.variable;

/**
 * @author neo
 */
final class EntityDecoderBuilder<T> {
    final Map<String, String> methods = new LinkedHashMap<>();
    final List<String> fields = Lists.newArrayList();
    private final Class<T> entityClass;
    private final Set<Class<? extends Enum<?>>> enumClasses = Sets.newHashSet();
    private final String helper = EntityCodecHelper.class.getCanonicalName();

    EntityDecoderBuilder(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public EntityDecoder<T> build() {
        DynamicInstanceBuilder<EntityDecoder<T>> builder = new DynamicInstanceBuilder<>(EntityDecoder.class, EntityDecoder.class.getCanonicalName() + "$" + entityClass.getSimpleName());
        fields.add("private final " + Logger.class.getCanonicalName() + " logger = " + LoggerFactory.class.getCanonicalName() + ".getLogger(" + EntityDecoder.class.getCanonicalName() + ".class);\n");
        buildMethods();
        fields.forEach(builder::addField);
        methods.values().forEach(builder::addMethod);
        return builder.build();
    }

    private void buildMethods() {
        String methodName = decodeEntityMethod(entityClass);
        CodeBuilder builder = new CodeBuilder()
                .append("public Object decode(org.bson.BsonReader reader) {\n")
                .indent(1).append("return {}(reader, \"\");\n", methodName)
                .append("}");

        methods.put("decode", builder.build());
    }

    private String decodeEntityMethod(Class<?> entityClass) {
        String entityClassName = entityClass.getCanonicalName();
        String methodName = "decode_" + entityClassName.replace('.', '_');
        if (methods.containsKey(methodName)) return methodName;

        CodeBuilder builder = new CodeBuilder().append("public {} {}(org.bson.BsonReader reader, String parentField) {\n", entityClassName, methodName);
        builder.indent(1).append("org.bson.BsonType currentType = reader.getCurrentBsonType();\n");

        builder.indent(1).append("if (currentType != null && currentType == org.bson.BsonType.NULL) {\n");
        builder.indent(2).append("reader.readNull();\n");
        builder.indent(2).append("return null;\n");
        builder.indent(1).append("}\n");

        builder.indent(1).append("if (currentType != null && currentType != org.bson.BsonType.DOCUMENT) {\n");
        builder.indent(2).append("logger.warn(\"unexpected field type, field={}, type={}\", parentField, currentType);\n");
        builder.indent(2).append("reader.skipValue();\n");
        builder.indent(2).append("return null;\n");
        builder.indent(1).append("}\n");

        builder.indent(1).append("{} entity = new {}();\n", entityClassName, entityClassName);

        builder.indent(1).append("reader.readStartDocument();\n")
               .indent(1).append("while (reader.readBsonType() != org.bson.BsonType.END_OF_DOCUMENT) {\n")
               .indent(2).append("String fieldName = reader.readName();\n")
               .indent(2).append("String fieldPath = parentField + \".\" + fieldName;\n");

        for (Field field : Classes.instanceFields(entityClass)) {
            decodeEntityField(builder, field);
        }

        builder.indent(2).append("logger.warn(\"undefined field, field={}, type={}\", fieldPath, reader.getCurrentBsonType());\n");
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
            builder.indent(3).append("{} = ({}) {}.decode(reader, null);\n", fieldVariable, type(fieldClass), enumCodecVariable);
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
        String valueClassName = valueClass.getCanonicalName();
        String methodName = ("decode_" + Map.class.getCanonicalName() + "_" + valueClassName).replace('.', '_');
        if (methods.containsKey(methodName)) return methodName;

        CodeBuilder builder = new CodeBuilder();
        builder.append("private java.util.Map {}(org.bson.BsonReader reader, String parentField) {\n", methodName);
        builder.indent(1).append("org.bson.BsonType currentType = reader.getCurrentBsonType();\n");
        builder.indent(1).append("if (currentType == org.bson.BsonType.NULL) { reader.readNull(); return null; }\n");
        builder.indent(1).append("if (currentType != org.bson.BsonType.DOCUMENT) {\n");
        builder.indent(2).append("logger.warn(\"unexpected field type, field={}, type={}\", parentField, currentType);\n");
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
        builder.append("}\n");

        methods.put(methodName, builder.build());
        return methodName;
    }

    private String decodeListMethod(Class<?> valueClass) {
        String valueClassName = valueClass.getCanonicalName();
        String methodName = ("decode_" + List.class.getCanonicalName() + "_" + valueClassName).replace('.', '_');
        if (methods.containsKey(methodName)) return methodName;

        CodeBuilder builder = new CodeBuilder();
        builder.append("private java.util.List {}(org.bson.BsonReader reader, String fieldPath) {\n", methodName);
        builder.indent(1).append("org.bson.BsonType currentType = reader.getCurrentBsonType();\n");

        builder.indent(1).append("if (currentType == org.bson.BsonType.NULL) {\n");
        builder.indent(2).append("reader.readNull();\n");
        builder.indent(2).append("return null;\n");
        builder.indent(1).append("}\n");

        builder.indent(1).append("if (currentType != org.bson.BsonType.ARRAY) {\n");
        builder.indent(2).append("logger.warn(\"unexpected field type, field={}, type={}\", fieldPath, currentType);\n");
        builder.indent(2).append("reader.skipValue();\n");
        builder.indent(2).append("return null;\n");
        builder.indent(1).append("}\n");

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
            builder.indent(2).append("list.add({}.read(reader, fieldPath));\n", enumCodecVariable, valueClassName);
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
        builder.append("}\n");

        methods.put(methodName, builder.build());
        return methodName;
    }

    private String registerEnumCodec(Class<?> fieldClass) {
        @SuppressWarnings("unchecked")
        boolean added = enumClasses.add((Class<? extends Enum<?>>) fieldClass);
        String fieldVariable = fieldClass.getCanonicalName().replace('.', '_') + "Codec";
        if (added) {
            String enumCodecTypeLiteral = type(EnumCodec.class);
            String field = Strings.format("private final {} {} = new {}({});\n",
                    enumCodecTypeLiteral,
                    fieldVariable,
                    enumCodecTypeLiteral,
                    variable(fieldClass));
            fields.add(field);
        }
        return fieldVariable;
    }
}
