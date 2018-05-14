package core.framework.mongo.impl;

import core.framework.impl.asm.CodeBuilder;
import core.framework.impl.asm.DynamicInstanceBuilder;
import core.framework.impl.reflect.Classes;
import core.framework.impl.reflect.GenericTypes;
import core.framework.mongo.Id;
import core.framework.util.Maps;
import core.framework.util.Types;
import org.bson.types.ObjectId;

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
final class EntityEncoderBuilder<T> {
    final Map<Class<? extends Enum<?>>, String> enumCodecFields = Maps.newHashMap();
    final DynamicInstanceBuilder<EntityEncoder<T>> builder;
    private final Class<T> entityClass;
    private final Map<Type, String> encodeMethods = Maps.newHashMap();
    private int index;

    EntityEncoderBuilder(Class<T> entityClass) {
        this.entityClass = entityClass;
        builder = new DynamicInstanceBuilder<>(EntityEncoder.class, EntityEncoder.class.getCanonicalName() + "$" + entityClass.getSimpleName());
    }

    public EntityEncoder<T> build() {
        String methodName = encodeEntityMethod(entityClass);
        CodeBuilder builder = new CodeBuilder();
        builder.append("public void encode(org.bson.BsonWriter writer, Object entity) {\n")
               .indent(1).append("{} wrapper = new {}(writer);\n", type(BsonWriterWrapper.class), type(BsonWriterWrapper.class))
               .indent(1).append("{}(writer, wrapper, ({}) entity);\n", methodName, entityClass.getCanonicalName())
               .append("}");
        this.builder.addMethod(builder.build());
        return this.builder.build();
    }

    private String encodeEntityMethod(Class<?> entityClass) {
        String methodName = encodeMethods.get(entityClass);
        if (methodName != null) return methodName;

        methodName = "encode" + entityClass.getSimpleName() + (index++);
        CodeBuilder builder = new CodeBuilder();
        builder.append("private void {}(org.bson.BsonWriter writer, {} wrapper, {} entity) {\n", methodName, type(BsonWriterWrapper.class), type(entityClass));
        builder.indent(1).append("writer.writeStartDocument();\n");
        for (Field field : Classes.instanceFields(entityClass)) {
            String fieldVariable = "entity." + field.getName();
            builder.indent(1).append("writer.writeName({});\n", variable(mongoField(field)));
            encodeField(builder, fieldVariable, field.getGenericType(), 1);
        }
        builder.indent(1).append("writer.writeEndDocument();\n")
               .append('}');
        this.builder.addMethod(builder.build());

        encodeMethods.put(entityClass, methodName);
        return methodName;
    }

    private String mongoField(Field field) {
        if (field.isAnnotationPresent(Id.class)) return "_id";
        return field.getDeclaredAnnotation(core.framework.mongo.Field.class).name();
    }

    private String encodeListMethod(Class<?> valueClass) {
        String methodName = encodeMethods.get(Types.list(valueClass));
        if (methodName != null) return methodName;

        methodName = "encodeList" + valueClass.getSimpleName() + (index++);
        CodeBuilder builder = new CodeBuilder();
        builder.append("private void {}(org.bson.BsonWriter writer, {} wrapper, java.util.List list) {\n", methodName, type(BsonWriterWrapper.class));
        builder.indent(1).append("writer.writeStartArray();\n")
               .indent(1).append("for (java.util.Iterator iterator = list.iterator(); iterator.hasNext(); ) {\n")
               .indent(2).append("{} value = ({}) iterator.next();\n", type(valueClass), type(valueClass));

        encodeField(builder, "value", valueClass, 2);

        builder.indent(1).append("}\n")
               .indent(1).append("writer.writeEndArray();\n")
               .append('}');
        this.builder.addMethod(builder.build());

        encodeMethods.put(Types.list(valueClass), methodName);
        return methodName;
    }

    private String encodeMapMethod(Class<?> valueClass) {
        String methodName = encodeMethods.get(Types.map(String.class, valueClass));
        if (methodName != null) return methodName;

        methodName = "encodeMap" + valueClass.getSimpleName() + (index++);
        CodeBuilder builder = new CodeBuilder();
        builder.append("private void {}(org.bson.BsonWriter writer, {} wrapper, java.util.Map map) {\n", methodName, type(BsonWriterWrapper.class));
        builder.indent(1).append("writer.writeStartDocument();\n")
               .indent(1).append("for (java.util.Iterator iterator = map.entrySet().iterator(); iterator.hasNext(); ) {\n")
               .indent(2).append("java.util.Map.Entry entry = (java.util.Map.Entry) iterator.next();\n")
               .indent(2).append("String key = (String) entry.getKey();\n")
               .indent(2).append("{} value = ({}) entry.getValue();\n", type(valueClass), type(valueClass))
               .indent(2).append("writer.writeName(key);\n");

        encodeField(builder, "value", valueClass, 2);

        builder.indent(1).append("}\n")
               .indent(1).append("writer.writeEndDocument();\n")
               .append('}');
        this.builder.addMethod(builder.build());

        encodeMethods.put(Types.map(String.class, valueClass), methodName);
        return methodName;
    }

    private void encodeField(CodeBuilder builder, String fieldVariable, Type fieldType, int indent) {
        Class<?> fieldClass = GenericTypes.rawClass(fieldType);

        if (String.class.equals(fieldClass)
                || Number.class.isAssignableFrom(fieldClass)
                || LocalDateTime.class.equals(fieldClass)
                || ZonedDateTime.class.equals(fieldClass)
                || Boolean.class.equals(fieldClass)
                || ObjectId.class.equals(fieldClass)) {
            builder.indent(indent).append("wrapper.write({});\n", fieldVariable);
        } else if (fieldClass.isEnum()) {
            String enumCodecVariable = registerEnumCodec(fieldClass);
            builder.indent(indent).append("{}.encode(writer, {}, null);\n", enumCodecVariable, fieldVariable);
        } else if (GenericTypes.isGenericList(fieldType)) {
            String methodName = encodeListMethod(GenericTypes.listValueClass(fieldType));
            builder.indent(indent).append("if ({} == null) writer.writeNull();\n", fieldVariable);
            builder.indent(indent).append("else {}(writer, wrapper, {});\n", methodName, fieldVariable);
        } else if (GenericTypes.isGenericStringMap(fieldType)) {
            String methodName = encodeMapMethod(GenericTypes.mapValueClass(fieldType));
            builder.indent(indent).append("if ({} == null) writer.writeNull();\n", fieldVariable);
            builder.indent(indent).append("else {}(writer, wrapper, {});\n", methodName, fieldVariable);
        } else {
            String encodeFieldMethod = encodeEntityMethod(fieldClass);
            builder.indent(indent).append("if ({} == null) writer.writeNull();\n", fieldVariable);
            builder.indent(indent).append("else {}(writer, wrapper, {});\n", encodeFieldMethod, fieldVariable);
        }
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
