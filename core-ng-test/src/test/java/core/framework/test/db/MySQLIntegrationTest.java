package core.framework.test.db;

import core.framework.db.Database;
import core.framework.db.Query;
import core.framework.db.Repository;
import core.framework.inject.Inject;
import core.framework.test.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class MySQLIntegrationTest extends IntegrationTest {
    @Inject
    Database database;
    @Inject
    Repository<TestDBEntity> repository;

    @BeforeEach
    void truncateTable() {
        database.execute("TRUNCATE TABLE test_entity");
    }

    @Test
    void insert() {
        TestDBEntity entity = new TestDBEntity();
        entity.id = UUID.randomUUID().toString();
        entity.dateTimeField = LocalDateTime.now();
        entity.dateField = LocalDate.now();
        entity.zonedDateTimeField = ZonedDateTime.now();
        repository.insert(entity);

        TestDBEntity selectedEntity = repository.get(entity.id).get();
        assertEquals(entity.dateField, selectedEntity.dateField);
        assertEquals(entity.dateTimeField, selectedEntity.dateTimeField);
        assertEquals(entity.zonedDateTimeField, selectedEntity.zonedDateTimeField);
    }

    @Test
    void select() {
        createTestEntities();

        Query<TestDBEntity> query = repository.select();
        query.where("int_field > ?", 3)
             .where("string_field like ?", "value%")
             .orderBy("int_field")
             .limit(5);

        int count = query.count();
        assertEquals(26, count);

        List<TestDBEntity> entities = query.fetch();
        assertEquals(5, entities.size());
        assertEquals(4, (int) entities.get(0).intField);
    }

    @Test
    void selectWithWhere() {
        createTestEntities();

        List<TestDBEntity> entities = repository.select("string_field = ?", "value-10");
        assertEquals(1, entities.size());
        assertEquals(10, (int) entities.get(0).intField);
    }

    private void createTestEntities() {
        for (int i = 0; i < 30; i++) {
            TestDBEntity entity = new TestDBEntity();
            entity.id = UUID.randomUUID().toString();
            entity.intField = i;
            entity.stringField = "value-" + i;
            repository.insert(entity);
        }
    }
}
