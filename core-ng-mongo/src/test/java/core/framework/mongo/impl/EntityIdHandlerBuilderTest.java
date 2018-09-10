package core.framework.mongo.impl;

import core.framework.util.ClasspathResources;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EntityIdHandlerBuilderTest {
    private EntityIdHandlerBuilder<TestEntity> builder;
    private EntityIdHandler<TestEntity> handler;

    @BeforeAll
    void createEntityIdHandler() {
        builder = new EntityIdHandlerBuilder<>(TestEntity.class);
        handler = builder.build();
    }

    @Test
    void sourceCode() {
        String sourceCode = builder.builder.sourceCode();
        assertEquals(ClasspathResources.text("mongo-test/entity-id-handler.java"), sourceCode);
    }

    @Test
    void set() {
        var id = new ObjectId();
        TestEntity entity = new TestEntity();
        handler.set(entity, id);

        assertEquals(id, entity.id);
    }

    @Test
    void testGet() {
        var entity = new TestEntity();
        entity.id = new ObjectId();
        ObjectId id = (ObjectId) handler.get(entity);

        assertEquals(entity.id, id);
    }
}
