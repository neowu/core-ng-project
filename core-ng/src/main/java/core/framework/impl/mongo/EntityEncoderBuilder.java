package core.framework.impl.mongo;

import core.framework.api.mongo.Id;
import core.framework.api.util.Lists;
import core.framework.api.util.Sets;
import core.framework.api.util.Strings;
import core.framework.impl.code.CodeBuilder;
import core.framework.impl.code.DynamicInstanceBuilder;
import core.framework.impl.reflect.Classes;
import core.framework.impl.reflect.GenericTypes;
import org.bson.types.ObjectId;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author neo
 */
final class EntityEncoderBuilder<T> {
    final Map<String, String> methods = new LinkedHashMap<>();
    final Set<Class<? extends Enum<?>>> enumClasses = Sets.newHashSet();
    final List<String> fields = Lists.newArrayList();
    private final Class<T> entityClass;
    private final String helper = EntityCodecHelper.class.getCanonicalName();

    EntityEncoderBuilder(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public EntityEncoder<T> build() {
        DynamicInstanceBuilder<EntityEncoder<T>> builder = new DynamicInstanceBuilder<>(EntityEncoder.class, EntityEncoder.class.getCanonicalName() + "$" + entityClass.getSimpleName());
        buildMethods();
        fields.forEach(builder::addField);
        methods.values().forEach(builder::addMethod);
        return builder.build();
    }

    private void buildMethods() {
        String methodName = encodeEntityMethod(entityClass);
        CodeBuilder builder = new CodeBuilder().append("public void encode(org.bson.BsonWriter writer, Object entity) {\n")
                                               .indent(1).append("{}(writer, ({}) entity);\n", methodName, entityClass.getCanonicalName())
                                               .append("}");

        methods.put("encode", builder.build());
    }

    private String encodeEntityMethod(Class<?> entityClass) {
        String entityClassName = entityClass.getCanonicalName();
        String methodName = "encode_" + entityClassName.replaceAll("\\.", "_");
        if (methods.containsKey(methodName)) return methodName;

        CodeBuilder builder = new CodeBuilder().append("private void {}(org.bson.BsonWriter writer, {} entity) {\n", methodName, entityClassName);
        builder.indent(1).append("writer.writeStartDocument();\n");
        for (Field field : Classes.instanceFields(entityClass)) {
            Type fieldType = field.getGenericType();
            String fieldVariable = "entity." + field.getName();

            String mongoFieldName;
            if (field.isAnnotationPresent(Id.class)) mongoFieldName = "_id";
            else mongoFieldName = field.getDeclaredAnnotation(core.framework.api.mongo.Field.class).name();
            builder.indent(1).append("writer.writeName(\"{}\");\n", mongoFieldName);
            encodeField(builder, fieldVariable, fieldType, 1);
        }
        builder.indent(1).append("writer.writeEndDocument();\n");

        builder.append("}\n");

        methods.put(methodName, builder.build());
        return methodName;
    }

    private String encodeListMethod(Class<?> valueClass) {
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

        methods.put(methodName, builder.build());
        return methodName;
    }

    private String encodeMapMethod(Class<?> valueClass) {
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

        methods.put(methodName, builder.build());
        return methodName;
    }

    private void encodeField(CodeBuilder builder, String fieldVariable, Type fieldType, int indent) {
        Class<?> fieldClass = GenericTypes.rawClass(fieldType);

        if (String.class.equals(fieldClass)) {
            builder.indent(indent).append("{}.writeString(writer, {});\n", helper, fieldVariable);
        } else if (Integer.class.equals(fieldClass)) {
            builder.indent(indent).append("{}.writeInteger(writer, {});\n", helper, fieldVariable);
        } else if (Long.class.equals(fieldClass)) {
            builder.indent(indent).append("{}.writeLong(writer, {});\n", helper, fieldVariable);
        } else if (LocalDateTime.class.equals(fieldClass)) {
            builder.indent(indent).append("{}.writeLocalDateTime(writer, {});\n", helper, fieldVariable);
        } else if (ZonedDateTime.class.equals(fieldClass)) {
            builder.indent(indent).append("{}.writeZonedDateTime(writer, {});\n", helper, fieldVariable);
        } else if (fieldClass.isEnum()) {
            String enumCodecVariable = registerEnumCodec(fieldClass);
            builder.indent(indent).append("{}.encode(writer, {}, null);\n", enumCodecVariable, fieldVariable);
        } else if (Double.class.equals(fieldClass)) {
            builder.indent(indent).append("{}.writeDouble(writer, {});\n", helper, fieldVariable);
        } else if (Boolean.class.equals(fieldClass)) {
            builder.indent(indent).append("{}.writeBoolean(writer, {});\n", helper, fieldVariable);
        } else if (ObjectId.class.equals(fieldClass)) {
            builder.indent(indent).append("{}.writeObjectId(writer, {});\n", helper, fieldVariable);
        } else if (GenericTypes.isGenericList(fieldType)) {
            String methodName = encodeListMethod(GenericTypes.listValueClass(fieldType));
            builder.indent(indent).append("if ({} == null) writer.writeNull();\n", fieldVariable);
            builder.indent(indent).append("else {}(writer, {});\n", methodName, fieldVariable);
        } else if (GenericTypes.isGenericStringMap(fieldType)) {
            String methodName = encodeMapMethod(GenericTypes.mapValueClass(fieldType));
            builder.indent(indent).append("if ({} == null) writer.writeNull();\n", fieldVariable);
            builder.indent(indent).append("else {}(writer, {});\n", methodName, fieldVariable);
        } else {
            String encodeFieldMethod = encodeEntityMethod(fieldClass);
            builder.indent(indent).append("if ({} == null) writer.writeNull();\n", fieldVariable);
            builder.indent(indent).append("else {}(writer, {});\n", encodeFieldMethod, fieldVariable);
        }
    }

    private String registerEnumCodec(Class<?> fieldClass) {
        @SuppressWarnings("unchecked")
        boolean added = enumClasses.add((Class<? extends Enum<?>>) fieldClass);
        String fieldVariable = fieldClass.getCanonicalName().replaceAll("\\.", "_") + "Codec";
        if (added) {
            String field = Strings.format("private final {} {} = new {}({}.class);\n",
                EnumCodec.class.getCanonicalName(),
                fieldVariable,
                EnumCodec.class.getCanonicalName(),
                fieldClass.getCanonicalName());
            fields.add(field);
        }
        return fieldVariable;
    }
}
