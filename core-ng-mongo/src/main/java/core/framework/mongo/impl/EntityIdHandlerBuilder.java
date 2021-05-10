package core.framework.mongo.impl;

import core.framework.internal.asm.CodeBuilder;
import core.framework.internal.asm.DynamicInstanceBuilder;
import core.framework.internal.reflect.Classes;
import core.framework.mongo.Id;
import org.bson.types.ObjectId;

import java.lang.reflect.Field;

import static core.framework.internal.asm.Literal.type;

/**
 * @author neo
 */
final class EntityIdHandlerBuilder<T> {
    final DynamicInstanceBuilder<EntityIdHandler<T>> builder;
    private final Class<T> entityClass;
    private final Field idField;

    EntityIdHandlerBuilder(Class<T> entityClass) {
        this.entityClass = entityClass;
        idField = idField();
        builder = new DynamicInstanceBuilder<>(EntityIdHandler.class, entityClass.getSimpleName());
    }

    EntityIdHandler<T> build() {
        builder.addMethod(buildGetMethod());
        builder.addMethod(buildSetMethod());
        builder.addMethod(buildGenerateIdIfAbsentMethod());
        return builder.build();
    }

    private Field idField() {
        for (Field field : Classes.instanceFields(entityClass)) {
            if (field.isAnnotationPresent(Id.class)) return field;
        }
        throw new Error("can not find id field, class=" + entityClass.getCanonicalName());
    }

    private String buildGenerateIdIfAbsentMethod() {
        var builder = new CodeBuilder();
        builder.append("public boolean generateIdIfAbsent() {\n")
            .indent(1).append("return {};\n", ObjectId.class.equals(idField.getType()) ? "true" : "false")
            .append("}");
        return builder.build();
    }

    private String buildGetMethod() {
        var builder = new CodeBuilder();
        builder.append("public Object get(Object value) {\n")
            .indent(1).append("{} entity = ({}) value;\n", type(entityClass), type(entityClass))
            .indent(1).append("return entity.{};\n", idField.getName())
            .append("}");
        return builder.build();
    }

    private String buildSetMethod() {
        var builder = new CodeBuilder();
        builder.append("public void set(Object value, Object id) {\n")
            .indent(1).append("{} entity = ({}) value;\n", type(entityClass), type(entityClass))
            .indent(1).append("entity.{} = ({}) id;\n", idField.getName(), type(idField.getType()))
            .append("}");
        return builder.build();
    }
}
