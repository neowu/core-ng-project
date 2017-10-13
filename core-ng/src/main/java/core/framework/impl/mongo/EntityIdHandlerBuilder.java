package core.framework.impl.mongo;

import core.framework.impl.asm.CodeBuilder;
import core.framework.impl.asm.DynamicInstanceBuilder;
import core.framework.impl.reflect.Classes;
import core.framework.mongo.Id;
import core.framework.util.Exceptions;
import org.bson.types.ObjectId;

import java.lang.reflect.Field;

import static core.framework.impl.asm.Literal.type;

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
        builder = new DynamicInstanceBuilder<>(EntityIdHandler.class, EntityIdHandler.class.getCanonicalName() + "$" + entityClass.getSimpleName());
    }

    public EntityIdHandler<T> build() {
        builder.addMethod(getMethod());
        builder.addMethod(setMethod());
        builder.addMethod(generateIdIfAbsentMethod());
        return builder.build();
    }

    private Field idField() {
        for (Field field : Classes.instanceFields(entityClass)) {
            if (field.isAnnotationPresent(Id.class)) return field;
        }
        throw Exceptions.error("can not find id field, class={}", entityClass.getCanonicalName());
    }

    private String generateIdIfAbsentMethod() {
        CodeBuilder builder = new CodeBuilder();
        builder.append("public boolean generateIdIfAbsent() {\n")
               .indent(1).append("return {};\n", ObjectId.class.equals(idField.getType()) ? "true" : "false")
               .append("}");
        return builder.build();
    }

    private String getMethod() {
        CodeBuilder builder = new CodeBuilder();
        builder.append("public Object get(Object value) {\n")
               .indent(1).append("{} entity = ({}) value;\n", type(entityClass), type(entityClass))
               .indent(1).append("return entity.{};\n", idField.getName())
               .append("}");
        return builder.build();
    }

    private String setMethod() {
        CodeBuilder builder = new CodeBuilder();
        builder.append("public void set(Object value, Object id) {\n")
               .indent(1).append("{} entity = ({}) value;\n", type(entityClass), type(entityClass))
               .indent(1).append("entity.{} = ({}) id;\n", idField.getName(), type(idField.getType()))
               .append("}");
        return builder.build();
    }
}
