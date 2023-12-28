package core.framework.mongo.impl;

import org.bson.BsonObjectId;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author neo
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EntityCodecTest {
    private EntityCodec<TestEntity> entityCodec;

    @BeforeAll
    void createEntityCodec() {
        var entityCodecs = new EntityCodecs();
        entityCodecs.registerEntity(TestEntity.class);
        entityCodec = (EntityCodec<TestEntity>) entityCodecs.codecRegistry().get(TestEntity.class);
    }

    @Test
    void documentHasId() {
        assertFalse(entityCodec.documentHasId(new TestEntity()));

        var entity = new TestEntity();
        entity.id = new ObjectId();
        assertTrue(entityCodec.documentHasId(entity));
    }

    @Test
    void generateIdIfAbsentFromDocument() {
        var entity = new TestEntity();
        entityCodec.generateIdIfAbsentFromDocument(entity);
        assertNotNull(entity.id);
    }

    @Test
    void getDocumentId() {
        var entity = new TestEntity();
        entity.id = new ObjectId();
        assertEquals(new BsonObjectId(entity.id), entityCodec.getDocumentId(entity));
    }
}
