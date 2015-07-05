package core.framework.impl.mongo;

import core.framework.impl.codegen.DynamicInstanceBuilder;

/**
 * @author neo
 */
public class EntityIdHandlerBuilder<T> {
    private final Class<T> entityClass;

    public EntityIdHandlerBuilder(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public EntityIdHandler<T> build() {
        DynamicInstanceBuilder<EntityIdHandler<T>> builder = new DynamicInstanceBuilder<>(EntityIdHandler.class, EntityIdHandler.class.getCanonicalName() + "$" + entityClass.getCanonicalName());
        builder.addMethod(getMethod());
        builder.addMethod(setMethod());
        return builder.build();
    }

    String getMethod() {
        return "public org.bson.types.ObjectId get(Object value) {\n"
            + "    " + entityClass.getCanonicalName() + " entity = (" + entityClass.getCanonicalName() + ") value;\n"
            + "    return entity.id;\n"
            + '}';
    }

    String setMethod() {
        return "public void set(Object value, org.bson.types.ObjectId id) {\n"
            + "    " + entityClass.getCanonicalName() + " entity = (" + entityClass.getCanonicalName() + ") value;\n"
            + "    entity.id = id;\n"
            + '}';
    }
}
