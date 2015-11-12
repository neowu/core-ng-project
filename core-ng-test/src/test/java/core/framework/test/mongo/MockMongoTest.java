package core.framework.test.mongo;

import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

/**
 * @author neo
 */
public class MockMongoTest {
    MockMongo mongo;

    @Before
    public void createMockMongo() {
        mongo = new MockMongo();
        mongo.entityClass(TestEntity.class);
    }

    @Test
    public void insert() {
        TestEntity entity = new TestEntity();
        entity.stringField = "string";
        mongo.insert(entity);

        Assert.assertNotNull(entity.id);

        Optional<TestEntity> loadedEntity = mongo.get(TestEntity.class, entity.id);
        Assert.assertTrue(loadedEntity.isPresent());
        Assert.assertEquals(entity.stringField, loadedEntity.get().stringField);
    }

    @Test
    public void update() {
        TestEntity entity = new TestEntity();
        entity.id = new ObjectId();
        entity.stringField = "value1";
        mongo.update(entity);

        TestEntity loadedEntity = mongo.get(TestEntity.class, entity.id).get();
        Assert.assertEquals(entity.stringField, loadedEntity.stringField);

        entity.stringField = "value2";
        mongo.update(entity);

        loadedEntity = mongo.get(TestEntity.class, entity.id).get();
        Assert.assertEquals(entity.stringField, loadedEntity.stringField);
    }
}