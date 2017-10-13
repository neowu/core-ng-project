package core.framework.test.db;

import core.framework.db.Database;
import core.framework.db.Query;
import core.framework.db.Repository;
import core.framework.test.IntegrationTest;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * @author neo
 */
public class MySQLIntegrationTest extends IntegrationTest {
    @Inject
    Database database;
    @Inject
    Repository<TestDBEntity> repository;

    @Before
    public void truncateTable() {
        database.execute("TRUNCATE TABLE test_entity");
    }

    @Test
    public void insert() {
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
    public void select() {
        for (int i = 0; i < 30; i++) {
            TestDBEntity entity = new TestDBEntity();
            entity.id = UUID.randomUUID().toString();
            entity.intField = i;
            entity.stringField = "value-" + i;
            repository.insert(entity);
        }

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
}
