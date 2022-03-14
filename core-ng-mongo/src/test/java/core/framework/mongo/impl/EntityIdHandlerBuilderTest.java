package core.framework.mongo.impl;

import core.framework.util.ClasspathResources;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(sourceCode).isEqualToIgnoringNewLines(ClasspathResources.text("mongo-test/entity-id-handler.java"));
    }

    @Test
    void set() {
        var id = new ObjectId();
        TestEntity entity = new TestEntity();
        handler.set(entity, id);

        assertThat(entity.id).isEqualTo(id);
    }

    @Test
    void get() {
        var entity = new TestEntity();
        entity.id = new ObjectId();
        ObjectId id = (ObjectId) handler.get(entity);

        assertThat(entity.id).isEqualTo(id);
    }
}
