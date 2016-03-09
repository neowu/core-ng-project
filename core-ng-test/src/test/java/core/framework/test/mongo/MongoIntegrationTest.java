package core.framework.test.mongo;

import com.mongodb.client.model.Filters;
import core.framework.api.mongo.Mongo;
import core.framework.api.mongo.MongoCollection;
import core.framework.test.IntegrationTest;
import org.bson.types.ObjectId;
import org.junit.After;
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
    MongoCollection<TestEntity> testEntityCollection;
    @Inject
    Mongo mongo;

    @After
    public void cleanup() {
        mongo.dropCollection("entity");
    }

    @Test
    public void insert() {
        TestEntity entity = new TestEntity();
        entity.stringField = "string";
        testEntityCollection.insert(entity);

        assertNotNull(entity.id);

        Optional<TestEntity> loadedEntity = testEntityCollection.get(entity.id);
        assertTrue(loadedEntity.isPresent());
        assertEquals(entity.stringField, loadedEntity.get().stringField);
    }

    @Test
    public void replace() {
        TestEntity entity = new TestEntity();
        entity.id = new ObjectId();
        entity.stringField = "value1";
        testEntityCollection.replace(entity);

        TestEntity loadedEntity = testEntityCollection.get(entity.id).get();
        assertEquals(entity.stringField, loadedEntity.stringField);

        entity.stringField = "value2";
        testEntityCollection.replace(entity);

        loadedEntity = testEntityCollection.get(entity.id).get();
        assertEquals(entity.stringField, loadedEntity.stringField);
    }

    @Test
    public void search() {
        TestEntity entity = new TestEntity();
        entity.id = new ObjectId();
        entity.stringField = "value";
        testEntityCollection.insert(entity);

        List<TestEntity> entities = testEntityCollection.find(Filters.eq("string_field", "value"));
        assertEquals(1, entities.size());
        assertEquals(entity.id, entities.get(0).id);
        assertEquals(entity.stringField, entities.get(0).stringField);
    }

    @Test
    public void searchByEnum() {
        TestEntity entity = new TestEntity();
        entity.id = new ObjectId();
        entity.stringField = "value";
        entity.enumField = TestEntity.TestEnum.VALUE1;
        testEntityCollection.insert(entity);

        List<TestEntity> entities = testEntityCollection.find(Filters.eq("enum_field", TestEntity.TestEnum.VALUE1));
        assertEquals(1, entities.size());
        assertEquals(entity.id, entities.get(0).id);
        assertEquals(entity.stringField, entities.get(0).stringField);
        assertEquals(entity.enumField, entities.get(0).enumField);
    }
}
