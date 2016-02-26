package core.framework.test.mongo;

import com.mongodb.client.model.Filters;
import core.framework.api.mongo.Mongo;
import core.framework.test.IntegrationTest;
import org.bson.types.ObjectId;
import org.junit.Test;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author neo
 */
public class MongoIntegrationTest extends IntegrationTest {
    @Inject
    Mongo mongo;

    @Test
    public void insert() {
        TestEntity entity = new TestEntity();
        entity.stringField = "string";
        mongo.insert(entity);

        assertNotNull(entity.id);

        Optional<TestEntity> loadedEntity = mongo.get(TestEntity.class, entity.id);
        assertTrue(loadedEntity.isPresent());
        assertEquals(entity.stringField, loadedEntity.get().stringField);
    }

    @Test
    public void update() {
        TestEntity entity = new TestEntity();
        entity.id = new ObjectId();
        entity.stringField = "value1";
        mongo.update(entity);

        TestEntity loadedEntity = mongo.get(TestEntity.class, entity.id).get();
        assertEquals(entity.stringField, loadedEntity.stringField);

        entity.stringField = "value2";
        mongo.update(entity);

        loadedEntity = mongo.get(TestEntity.class, entity.id).get();
        assertEquals(entity.stringField, loadedEntity.stringField);
    }

    @Test
    public void search() {
        TestEntity entity = new TestEntity();
        entity.id = new ObjectId();
        entity.stringField = "value";
        mongo.insert(entity);

        List<TestEntity> entities = mongo.find(TestEntity.class, Filters.eq("string_field", "value"));
        assertEquals(1, entities.size());
        assertEquals(entity.id, entities.get(0).id);
        assertEquals(entity.stringField, entities.get(0).stringField);
    }
}
