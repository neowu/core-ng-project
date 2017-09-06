package core.framework.impl.mongo;

import core.framework.api.util.ClasspathResources;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author neo
 */
public class EntityIdHandlerBuilderTest {
    private EntityIdHandlerBuilder<TestEntity> builder;
    private EntityIdHandler<TestEntity> handler;

    @Before
    public void createEntityIdHandler() {
        builder = new EntityIdHandlerBuilder<>(TestEntity.class);
        handler = builder.build();
    }

    @Test
    public void sourceCode() {
        String sourceCode = builder.builder.sourceCode();
        assertEquals(ClasspathResources.text("mongo-test/entity-id-handler.java"), sourceCode);
    }

    @Test
    public void setId() {
        ObjectId id = new ObjectId();
        TestEntity entity = new TestEntity();
        handler.set(entity, id);

        Assert.assertEquals(id, entity.id);
    }

    @Test
    public void getId() {
        TestEntity entity = new TestEntity();
        entity.id = new ObjectId();
        ObjectId id = (ObjectId) handler.get(entity);

        Assert.assertEquals(entity.id, id);
    }
}
