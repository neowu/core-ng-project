package core.framework.impl.mongo;

import core.framework.api.mongo.Id;
import core.framework.impl.codegen.CodeBuilder;
import core.framework.impl.codegen.DynamicInstanceBuilder;
import org.bson.types.ObjectId;

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
public class EntityEncoderBuilder<T> {
    private final Class<T> entityClass;
    private final String helper = EntityCodecHelper.class.getCanonicalName();

    final Map<String, String> methods = new LinkedHashMap<>();

    public EntityEncoderBuilder(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public EntityEncoder<T> build() {
        DynamicInstanceBuilder<EntityEncoder<T>> builder = new DynamicInstanceBuilder<>(EntityEncoder.class, EntityEncoder.class.getCanonicalName() + "$" + entityClass.getCanonicalName());
        buildMethods();
        methods.values().forEach(builder::addMethod);
        return builder.build();
    }

    private void buildMethods() {
        String methodName = encodeEntityMethod(entityClass);
        CodeBuilder builder = new CodeBuilder().append("public void encode(org.bson.BsonWriter writer, Object entity) {\n")
            .indent(1).append("{}(writer, ({}) entity);\n", methodName, entityClass.getCanonicalName())
            .append("}");

        methods.put("encode", builder.toString());
    }

    private String encodeEntityMethod(Class entityClass) {
        String entityClassName = entityClass.getCanonicalName();
        String methodName = "encode_" + entityClassName.replaceAll("\\.", "_");
        if (methods.containsKey(methodName)) return methodName;

        CodeBuilder builder = new CodeBuilder().append("private void {}(org.bson.BsonWriter writer, {} entity) {\n", methodName, entityClassName);
        builder.indent(1).append("writer.writeStartDocument();\n");
        for (Field field : entityClass.getFields()) {
            Type fieldType = field.getGenericType();
            String fieldVariable = "entity." + field.getName();
            if (field.isAnnotationPresent(Id.class)) {
                builder.indent(1).append("if ({} != null) writer.writeObjectId(\"_id\", {});\n", fieldVariable, fieldVariable);
            } else {
                String fieldName = field.getDeclaredAnnotation(core.framework.api.mongo.Field.class).name();
                builder.indent(1).append("writer.writeName(\"{}\");\n", fieldName);
                encodeField(builder, fieldVariable, fieldType, 1);
            }
        }
        builder.indent(1).append("writer.writeEndDocument();\n");

        builder.append("}\n");

        methods.put(methodName, builder.toString());
        return methodName;
    }

    private String encodeListMethod(Class valueClass) {
        String valueClassName = valueClass.getCanonicalName();
        String methodName = ("encode_" + List.class.getCanonicalName() + "_" + valueClassName).replaceAll("\\.", "_");
        if (methods.containsKey(methodName)) return methodName;

        CodeBuilder builder = new CodeBuilder();
        builder.append("private void {}(org.bson.BsonWriter writer, java.util.List list) {\n", methodName);
        builder.indent(1).append("writer.writeStartArray();\n")
            .indent(1).append("for (java.util.Iterator iterator = list.iterator(); iterator.hasNext(); ) {\n")
            .indent(2).append("{} value = ({}) iterator.next();\n", valueClassName, valueClassName);

        encodeField(builder, "value", valueClass, 2);

        builder.indent(1).append("}\n")
            .indent(1).append("writer.writeEndArray();\n")
            .append("}\n");

        methods.put(methodName, builder.toString());
        return methodName;
    }

    private String encodeMapMethod(Class valueClass) {
        String valueClassName = valueClass.getCanonicalName();
        String methodName = ("encode_" + Map.class.getCanonicalName() + "_" + valueClassName).replaceAll("\\.", "_");
        if (methods.containsKey(methodName)) return methodName;

        CodeBuilder builder = new CodeBuilder();
        builder.append("private void {}(org.bson.BsonWriter writer, java.util.Map map) {\n", methodName);
        builder.indent(1).append("writer.writeStartDocument();\n")
            .indent(1).append("for (java.util.Iterator iterator = map.entrySet().iterator(); iterator.hasNext(); ) {\n")
            .indent(2).append("java.util.Map.Entry entry = (java.util.Map.Entry) iterator.next();\n")
            .indent(2).append("String key = (String) entry.getKey();\n")
            .indent(2).append("{} value = ({}) entry.getValue();\n", valueClassName, valueClassName)
            .indent(2).append("writer.writeName(key);\n");

        encodeField(builder, "value", valueClass, 2);

        builder.indent(1).append("}\n")
            .indent(1).append("writer.writeEndDocument();\n")
            .append("}\n");

        methods.put(methodName, builder.toString());
        return methodName;
    }

    private void encodeField(CodeBuilder builder, String fieldVariable, Type fieldType, int indent) {
        Class fieldClass = (Class) (fieldType instanceof Class ? fieldType : ((ParameterizedType) fieldType).getRawType());

        if (String.class.equals(fieldClass)) {
            builder.indent(indent).append("{}.writeString(writer, {});\n", helper, fieldVariable);
        } else if (Integer.class.equals(fieldClass)) {
            builder.indent(indent).append("{}.writeInteger(writer, {});\n", helper, fieldVariable);
        } else if (Long.class.equals(fieldClass)) {
            builder.indent(indent).append("{}.writeLong(writer, {});\n", helper, fieldVariable);
        } else if (LocalDateTime.class.equals(fieldClass)) {
            builder.indent(indent).append("{}.writeLocalDateTime(writer, {});\n", helper, fieldVariable);
        } else if (Enum.class.isAssignableFrom(fieldClass)) {
            builder.indent(indent).append("{}.writeEnum(writer, (Enum) {});\n", helper, fieldVariable);
        } else if (Double.class.equals(fieldClass)) {
            builder.indent(indent).append("{}.writeDouble(writer, {});\n", helper, fieldVariable);
        } else if (Boolean.class.equals(fieldClass)) {
            builder.indent(indent).append("{}.writeBoolean(writer, {});\n", helper, fieldVariable);
        } else if (ObjectId.class.equals(fieldClass)) {
            builder.indent(indent).append("{}.writeObjectId(writer, {});\n", helper, fieldVariable);
        } else if (List.class.equals(fieldClass) && fieldType instanceof ParameterizedType) {
            Class valueClass = (Class) ((ParameterizedType) fieldType).getActualTypeArguments()[0];
            String methodName = encodeListMethod(valueClass);
            builder.indent(indent).append("if ({} == null) writer.writeNull();\n", fieldVariable);
            builder.indent(indent).append("else {}(writer, {});\n", methodName, fieldVariable);
        } else if (Map.class.equals(fieldClass) && fieldType instanceof ParameterizedType) {
            Class valueClass = (Class) ((ParameterizedType) fieldType).getActualTypeArguments()[1];
            String methodName = encodeMapMethod(valueClass);
            builder.indent(indent).append("if ({} == null) writer.writeNull();\n", fieldVariable);
            builder.indent(indent).append("else {}(writer, {});\n", methodName, fieldVariable);
        } else {
            String encodeFieldMethod = encodeEntityMethod(fieldClass);
            builder.indent(indent).append("if ({} == null) writer.writeNull();\n", fieldVariable);
            builder.indent(indent).append("else {}(writer, {});\n", encodeFieldMethod, fieldVariable);
        }
    }
}
