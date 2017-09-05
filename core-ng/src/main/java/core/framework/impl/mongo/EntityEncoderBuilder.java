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
final class EntityEncoderBuilder<T> {
    final Map<String, String> methods = new LinkedHashMap<>();
    final Set<Class<? extends Enum<?>>> enumClasses = Sets.newHashSet();
    final List<String> fields = Lists.newArrayList();
    private final Class<T> entityClass;

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
        CodeBuilder builder = new CodeBuilder();
        builder.append("public void encode(org.bson.BsonWriter writer, Object entity) {\n")
               .indent(1).append("{} wrapper = new {}(writer);\n", type(BsonWriterWrapper.class), type(BsonWriterWrapper.class))
               .indent(1).append("{}(writer, wrapper, ({}) entity);\n", methodName, entityClass.getCanonicalName())
               .append("}");

        methods.put("encode", builder.build());
    }

    private String encodeEntityMethod(Class<?> entityClass) {
        String entityClassName = entityClass.getCanonicalName();
        String methodName = "encode_" + entityClassName.replace('.', '_');
        if (methods.containsKey(methodName)) return methodName;

        CodeBuilder builder = new CodeBuilder();
        builder.append("private void {}(org.bson.BsonWriter writer, {} wrapper, {} entity) {\n", methodName, type(BsonWriterWrapper.class), entityClassName);
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
        String methodName = ("encode_list_" + valueClassName).replace('.', '_');
        if (methods.containsKey(methodName)) return methodName;

        CodeBuilder builder = new CodeBuilder();
        builder.append("private void {}(org.bson.BsonWriter writer, {} wrapper, java.util.List list) {\n", methodName, type(BsonWriterWrapper.class));
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
        String methodName = ("encode_map_" + valueClassName).replace('.', '_');
        if (methods.containsKey(methodName)) return methodName;

        CodeBuilder builder = new CodeBuilder();
        builder.append("private void {}(org.bson.BsonWriter writer, {} wrapper, java.util.Map map) {\n", methodName, type(BsonWriterWrapper.class));
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
        boolean added = enumClasses.add((Class<? extends Enum<?>>) fieldClass);
        String fieldVariable = fieldClass.getCanonicalName().replace('.', '_') + "Codec";
        if (added) {
            String field = Strings.format("private final {} {} = new {}({});\n",
                    type(EnumCodec.class),
                    fieldVariable,
                    type(EnumCodec.class),
                    variable(fieldClass));
            fields.add(field);
        }
        return fieldVariable;
    }
}
