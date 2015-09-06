package core.framework.impl.mongo;

import core.framework.impl.code.CodeBuilder;
import core.framework.impl.code.DynamicInstanceBuilder;

/**
 * @author neo
 */
public final class EntityIdHandlerBuilder<T> {
    private final Class<T> entityClass;

    public EntityIdHandlerBuilder(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public EntityIdHandler<T> build() {
        DynamicInstanceBuilder<EntityIdHandler<T>> builder = new DynamicInstanceBuilder<>(EntityIdHandler.class, EntityIdHandler.class.getCanonicalName() + "$" + entityClass.getSimpleName());
        builder.addMethod(getMethod());
        builder.addMethod(setMethod());
        return builder.build();
    }

    private String getMethod() {
        CodeBuilder builder = new CodeBuilder();
        builder.append("public org.bson.types.ObjectId get(Object value) {\n")
            .indent(1).append("{} entity = ({}) value;\n", entityClass.getCanonicalName(), entityClass.getCanonicalName())
            .indent(1).append("return entity.id;\n")
            .append("}");
        return builder.build();
    }

    private String setMethod() {
        CodeBuilder builder = new CodeBuilder();
        builder.append("public void set(Object value, org.bson.types.ObjectId id) {\n")
            .indent(1).append("{} entity = ({}) value;\n", entityClass.getCanonicalName(), entityClass.getCanonicalName())
            .indent(1).append("entity.id = id;\n")
            .append("}");
        return builder.build();
    }
}
