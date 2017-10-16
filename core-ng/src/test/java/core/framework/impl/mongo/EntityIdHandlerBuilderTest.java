package core.framework.impl.mongo;

import core.framework.util.ClasspathResources;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class EntityIdHandlerBuilderTest {
    private EntityIdHandlerBuilder<TestEntity> builder;
    private EntityIdHandler<TestEntity> handler;

    @BeforeEach
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
    void setId() {
        ObjectId id = new ObjectId();
        TestEntity entity = new TestEntity();
        handler.set(entity, id);

        assertEquals(id, entity.id);
    }

    @Test
    void getId() {
        TestEntity entity = new TestEntity();
        entity.id = new ObjectId();
        ObjectId id = (ObjectId) handler.get(entity);

        assertEquals(entity.id, id);
    }
}
