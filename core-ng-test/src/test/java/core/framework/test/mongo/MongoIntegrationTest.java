package core.framework.test.mongo;

import com.mongodb.client.model.Filters;
import core.framework.inject.Inject;
import core.framework.mongo.Mongo;
import core.framework.mongo.MongoCollection;
import core.framework.test.IntegrationTest;
import core.framework.util.Lists;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author neo
 */
class MongoIntegrationTest extends IntegrationTest {
    @Inject
    MongoCollection<TestMongoEntity> testEntityCollection;
    @Inject
    Mongo mongo;

    @AfterEach
    void cleanup() {
        mongo.dropCollection("entity");
    }

    @Test
    void insert() {
        TestMongoEntity entity = new TestMongoEntity();
        entity.stringField = "string";
        entity.zonedDateTimeField = ZonedDateTime.of(LocalDateTime.of(2016, 9, 1, 11, 0, 0), ZoneId.of("UTC"));
        testEntityCollection.insert(entity);

        assertNotNull(entity.id);

        Optional<TestMongoEntity> loadedEntity = testEntityCollection.get(entity.id);
        assertTrue(loadedEntity.isPresent());
        assertEquals(entity.stringField, loadedEntity.get().stringField);
        assertEquals(entity.zonedDateTimeField.toInstant(), loadedEntity.get().zonedDateTimeField.toInstant());
    }

    @Test
    void replace() {
        TestMongoEntity entity = new TestMongoEntity();
        entity.id = new ObjectId();
        entity.stringField = "value1";
        testEntityCollection.replace(entity);

        TestMongoEntity loadedEntity = testEntityCollection.get(entity.id).orElseThrow(() -> new Error("not found"));
        assertEquals(entity.stringField, loadedEntity.stringField);

        entity.stringField = "value2";
        testEntityCollection.replace(entity);

        loadedEntity = testEntityCollection.get(entity.id).orElseThrow(() -> new Error("not found"));
        assertEquals(entity.stringField, loadedEntity.stringField);
    }

    @Test
    void find() {
        TestMongoEntity entity = createEntity("value2", TestMongoEntity.TestEnum.VALUE2);

        List<TestMongoEntity> entities = testEntityCollection.find(Filters.eq("string_field", "value2"));
        assertEquals(1, entities.size());
        assertEquals(entity.id, entities.get(0).id);
        assertEquals(entity.stringField, entities.get(0).stringField);
    }

    @Test
    void findByEnum() {
        TestMongoEntity entity = createEntity("value1", TestMongoEntity.TestEnum.VALUE1);

        List<TestMongoEntity> entities = testEntityCollection.find(Filters.eq("enum_field", TestMongoEntity.TestEnum.VALUE1));
        assertEquals(1, entities.size());
        assertEquals(entity.id, entities.get(0).id);
        assertEquals(entity.stringField, entities.get(0).stringField);
        assertEquals(entity.enumField, entities.get(0).enumField);
    }

    @Test
    void findOne() {
        TestMongoEntity entity = createEntity("value3", TestMongoEntity.TestEnum.VALUE1);

        Optional<TestMongoEntity> result = testEntityCollection.findOne(Filters.eq("string_field", "value3"));
        assertTrue(result.isPresent());
        assertEquals(entity.id, result.get().id);
        assertEquals(entity.stringField, result.get().stringField);
    }

    @Test
    void count() {
        long count = testEntityCollection.count(Filters.eq("string_field", "value"));

        assertEquals(0, count);
    }

    @Test
    void bulkInsert() {
        List<TestMongoEntity> entities = testEntities();
        testEntityCollection.bulkInsert(entities);

        for (TestMongoEntity entity : entities) {
            assertNotNull(entity.id);
        }

        Optional<TestMongoEntity> loadedEntity = testEntityCollection.get(entities.get(0).id);
        assertTrue(loadedEntity.isPresent());
        assertEquals(entities.get(0).stringField, loadedEntity.get().stringField);
    }

    @Test
    void bulkReplace() {
        List<TestMongoEntity> entities = testEntities();
        entities.forEach(entity -> entity.id = new ObjectId());
        testEntityCollection.bulkReplace(entities);

        Optional<TestMongoEntity> loadedEntity = testEntityCollection.get(entities.get(0).id);
        assertTrue(loadedEntity.isPresent());
        assertEquals(entities.get(0).stringField, loadedEntity.get().stringField);

        entities.get(0).stringField = "string1-updated";
        entities.get(1).stringField = "string2-updated";
        testEntityCollection.bulkReplace(entities);

        loadedEntity = testEntityCollection.get(entities.get(0).id);
        assertTrue(loadedEntity.isPresent());
        assertEquals(entities.get(0).stringField, loadedEntity.get().stringField);
    }

    @Test
    void bulkDelete() {
        List<TestMongoEntity> entities = testEntities();
        testEntityCollection.bulkInsert(entities);

        long deletedCount = testEntityCollection.bulkDelete(entities.stream().map(entity -> entity.id).collect(Collectors.toList()));
        assertEquals(2, deletedCount);
        assertFalse(testEntityCollection.get(entities.get(0).id).isPresent());
    }

    private TestMongoEntity createEntity(String stringField, TestMongoEntity.TestEnum enumField) {
        TestMongoEntity entity = new TestMongoEntity();
        entity.id = new ObjectId();
        entity.stringField = stringField;
        entity.enumField = enumField;
        testEntityCollection.insert(entity);
        return entity;
    }

    private List<TestMongoEntity> testEntities() {
        List<TestMongoEntity> entities = Lists.newArrayList();
        TestMongoEntity entity1 = new TestMongoEntity();
        entity1.stringField = "string1";
        entities.add(entity1);
        TestMongoEntity entity2 = new TestMongoEntity();
        entity2.stringField = "string2";
        entities.add(entity2);
        return entities;
    }
}
