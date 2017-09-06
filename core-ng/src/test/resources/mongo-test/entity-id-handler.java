public class EntityIdHandler$TestEntity implements core.framework.impl.mongo.EntityIdHandler {
    public Object get(Object value) {
        core.framework.impl.mongo.TestEntity entity = (core.framework.impl.mongo.TestEntity) value;
        return entity.id;
    }

    public void set(Object value, Object id) {
        core.framework.impl.mongo.TestEntity entity = (core.framework.impl.mongo.TestEntity) value;
        entity.id = (org.bson.types.ObjectId) id;
    }

    public boolean generateIdIfAbsent() {
        return true;
    }

}
