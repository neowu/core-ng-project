package core.framework.impl.mongo;

import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author neo
 */
public class EntityIdHandlerBuilderTest {
    @Test
    public void setId() {
        EntityIdHandler<TestEntity> handler = new EntityIdHandlerBuilder<>(TestEntity.class).build();

        ObjectId id = new ObjectId();
        TestEntity entity = new TestEntity();
        handler.set(entity, id);

        Assert.assertEquals(id, entity.id);
    }

    @Test
    public void getId() {
        EntityIdHandler<TestEntity> handler = new EntityIdHandlerBuilder<>(TestEntity.class).build();

        TestEntity entity = new TestEntity();
        entity.id = new ObjectId();
        ObjectId id = (ObjectId) handler.get(entity);

        Assert.assertEquals(entity.id, id);
    }
}