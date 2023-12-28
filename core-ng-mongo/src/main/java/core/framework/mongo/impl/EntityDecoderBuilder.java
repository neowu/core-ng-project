package core.framework.mongo.impl;

import core.framework.internal.asm.CodeBuilder;
import core.framework.internal.asm.DynamicInstanceBuilder;
import core.framework.internal.reflect.Classes;
import core.framework.internal.reflect.GenericTypes;
import core.framework.mongo.Id;
import core.framework.util.Maps;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Map;

import static core.framework.internal.asm.Literal.type;
import static core.framework.internal.asm.Literal.variable;

/**
 * @author neo
 */
final class EntityDecoderBuilder<T> {
    final DynamicInstanceBuilder<EntityDecoder<T>> builder;
    private final Class<T> entityClass;
    private final Map<Class<?>, String> enumCodecFields = Maps.newHashMap();
    private final Map<Type, String> decodeMethods = Maps.newHashMap();
    private int index;

    EntityDecoderBuilder(Class<T> entityClass) {
        this.entityClass = entityClass;
        builder = new DynamicInstanceBuilder<>(EntityDecoder.class, entityClass.getSimpleName());
    }

    EntityDecoder<T> build() {
        builder.addField("private final {} logger = {}.getLogger({});", type(Logger.class), type(LoggerFactory.class), variable(EntityDecoder.class));
        String methodName = decodeEntityMethod(entityClass);
        var builder = new CodeBuilder()
            .append("public Object decode(org.bson.BsonReader reader) {\n")
            .indent(1).append("{} wrapper = new {}(reader);\n", type(BsonReaderWrapper.class), type(BsonReaderWrapper.class))
            .indent(1).append("return {}(reader, wrapper, {});\n", methodName, variable(""))
            .append("}");
        this.builder.addMethod(builder.build());
        return this.builder.build();
    }

    private String decodeEntityMethod(Class<?> entityClass) {
        String methodName = decodeMethods.get(entityClass);
        if (methodName != null) return methodName;

        methodName = "decode" + entityClass.getSimpleName() + (index++);
        CodeBuilder builder = new CodeBuilder();
        builder.append("public {} {}(org.bson.BsonReader reader, {} wrapper, String parentField) {\n", type(entityClass), methodName, type(BsonReaderWrapper.class))
            .indent(1).append("boolean hasContent = wrapper.startReadEntity(parentField);\n")
            .indent(1).append("if (!hasContent) return null;\n")
            .indent(1).append("{} entity = new {}();\n", type(entityClass), type(entityClass))
            .indent(1).append("reader.readStartDocument();\n")
            .indent(1).append("while (reader.readBsonType() != org.bson.BsonType.END_OF_DOCUMENT) {\n")
            .indent(2).append("String fieldName = reader.readName();\n")
            .indent(2).append("String fieldPath = parentField + \".\" + fieldName;\n");

        builder.indent(2).append("switch (fieldName) {\n");
        for (Field field : Classes.instanceFields(entityClass)) {
            builder.indent(3).append("case {}: {\n", variable(mongoField(field)));

            String variable = decodeValue(builder, field.getGenericType(), 4);
            builder.indent(4).append("entity.{} = {};\n", field.getName(), variable);

            builder.indent(4).append("continue;\n")
                .indent(3).append("}\n");
        }
        builder.indent(2).append("}\n");    // not generate default branch to ignore unmatched field from mongo (to keep backward compatible, e.g. cleanup field)

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

    private String mongoField(Field field) {
        if (field.isAnnotationPresent(Id.class)) return "_id";
        return field.getDeclaredAnnotation(core.framework.mongo.Field.class).name();
    }

    private String decodeMapMethod(Type mapType) {
        String methodName = decodeMethods.get(mapType);
        if (methodName != null) return methodName;

        Class<?> keyClass = GenericTypes.mapKeyClass(mapType);
        Type valueType = GenericTypes.mapValueType(mapType);

        methodName = "decodeMap" + keyClass.getSimpleName() + GenericTypes.rawClass(valueType).getSimpleName() + (index++);
        CodeBuilder builder = new CodeBuilder();
        builder.append("private java.util.Map {}(org.bson.BsonReader reader, {} wrapper, String parentField) {\n", methodName, type(BsonReaderWrapper.class))
            .indent(1).append("java.util.Map map = wrapper.startReadMap(parentField);\n")
            .indent(1).append("if (map == null) return null;\n")
            .indent(1).append("reader.readStartDocument();\n")
            .indent(1).append("while (reader.readBsonType() != org.bson.BsonType.END_OF_DOCUMENT) {\n")
            .indent(2).append("String fieldName = reader.readName();\n")
            .indent(2).append("String fieldPath = parentField + \".\" + fieldName;\n");

        String variable = decodeValue(builder, valueType, 2);

        if (String.class.equals(keyClass)) {
            builder.indent(2).append("map.put(fieldName, {});\n", variable);
        } else if (keyClass.isEnum()) {
            String enumCodecVariable = registerEnumCodec(keyClass);
            builder.indent(2).append("map.put({}.decodeMapKey(fieldName), {});\n", enumCodecVariable, variable);
        } else {
            throw new Error("unknown key class, class=" + keyClass.getCanonicalName());
        }

        builder.indent(1).append("}\n")
            .indent(1).append("reader.readEndDocument();\n")
            .indent(1).append("return map;\n")
            .append('}');
        this.builder.addMethod(builder.build());

        decodeMethods.put(mapType, methodName);
        return methodName;
    }

    private String decodeListMethod(Type listType) {
        String methodName = decodeMethods.get(listType);
        if (methodName != null) return methodName;

        Class<?> valueClass = GenericTypes.listValueClass(listType);

        methodName = "decodeList" + valueClass.getSimpleName() + (index++);
        CodeBuilder builder = new CodeBuilder();
        builder.append("private java.util.List {}(org.bson.BsonReader reader, {} wrapper, String fieldPath) {\n", methodName, type(BsonReaderWrapper.class));
        builder.indent(1).append("java.util.List list = wrapper.startReadList(fieldPath);\n")
            .indent(1).append("if (list == null) return null;\n");
        builder.indent(1).append("reader.readStartArray();\n");
        builder.indent(1).append("while (reader.readBsonType() != org.bson.BsonType.END_OF_DOCUMENT) {\n");

        String variable = decodeValue(builder, valueClass, 2);
        builder.indent(2).append("list.add({});\n", variable);

        builder.indent(1).append("}\n");
        builder.indent(1).append("reader.readEndArray();\n");
        builder.indent(1).append("return list;\n");
        builder.append('}');
        this.builder.addMethod(builder.build());

        decodeMethods.put(listType, methodName);
        return methodName;
    }

    private String decodeValue(CodeBuilder builder, Type valueType, int indent) {
        String variable = "$" + (index++);
        builder.indent(indent);
        if (Integer.class.equals(valueType)) {
            builder.append("java.lang.Integer {} = wrapper.readInteger(fieldPath);\n", variable);
        } else if (String.class.equals(valueType)) {
            builder.append("java.lang.String {} = wrapper.readString(fieldPath);\n", variable);
        } else if (Long.class.equals(valueType)) {
            builder.append("java.lang.Long {} = wrapper.readLong(fieldPath);\n", variable);
        } else if (LocalDateTime.class.equals(valueType)) {
            builder.append("java.time.LocalDateTime {} = wrapper.readLocalDateTime(fieldPath);\n", variable);
        } else if (ZonedDateTime.class.equals(valueType)) {
            builder.append("java.time.ZonedDateTime {} = wrapper.readZonedDateTime(fieldPath);\n", variable);
        } else if (LocalDate.class.equals(valueType)) {
            builder.append("java.time.LocalDate {} = wrapper.readLocalDate(fieldPath);\n", variable);
        } else if (GenericTypes.rawClass(valueType).isEnum()) {
            Class<?> valueClass = GenericTypes.rawClass(valueType);
            String enumCodecVariable = registerEnumCodec(valueClass);
            builder.append("{} {} = ({}) {}.read(reader, fieldPath);\n", type(valueClass), variable, type(valueClass), enumCodecVariable);
        } else if (Double.class.equals(valueType)) {
            builder.append("java.lang.Double {} = wrapper.readDouble(fieldPath);\n", variable);
        } else if (BigDecimal.class.equals(valueType)) {
            builder.append("java.math.BigDecimal {} = wrapper.readBigDecimal(fieldPath);\n", variable);
        } else if (ObjectId.class.equals(valueType)) {
            builder.append("org.bson.types.ObjectId {} = wrapper.readObjectId(fieldPath);\n", variable);
        } else if (Boolean.class.equals(valueType)) {
            builder.append("java.lang.Boolean {} = wrapper.readBoolean(fieldPath);\n", variable);
        } else if (GenericTypes.isGenericList(valueType)) {
            String method = decodeListMethod(valueType);
            builder.append("java.util.List {} = {}(reader, wrapper, fieldPath);\n", variable, method);
        } else if (GenericTypes.isGenericMap(valueType)) {
            String method = decodeMapMethod(valueType);
            builder.append("java.util.Map {} = {}(reader, wrapper, fieldPath);\n", variable, method);
        } else {
            Class<?> valueClass = GenericTypes.rawClass(valueType);
            String method = decodeEntityMethod(valueClass);
            builder.append("{} {} = {}(reader, wrapper, fieldPath);\n", type(valueClass), variable, method);
        }
        return variable;
    }

    private String registerEnumCodec(Class<?> fieldClass) {
        return enumCodecFields.computeIfAbsent(fieldClass, key -> {
            String fieldVariable = "enumCodec" + fieldClass.getSimpleName() + (index++);
            builder.addField("private final {} {} = new {}({});", type(EnumCodec.class), fieldVariable, type(EnumCodec.class), variable(fieldClass));
            return fieldVariable;
        });
    }
}
