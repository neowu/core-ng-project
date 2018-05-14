public class EntityIdHandler$TestEntity implements core.framework.mongo.impl.EntityIdHandler {
    public Object get(Object value) {
        core.framework.mongo.impl.TestEntity entity = (core.framework.mongo.impl.TestEntity) value;
        return entity.id;
    }

    public void set(Object value, Object id) {
        core.framework.mongo.impl.TestEntity entity = (core.framework.mongo.impl.TestEntity) value;
        entity.id = (org.bson.types.ObjectId) id;
    }

    public boolean generateIdIfAbsent() {
        return true;
    }

}
