package core.framework.test.db;

import core.framework.db.Database;
import core.framework.db.Query;
import core.framework.db.Repository;
import core.framework.inject.Inject;
import core.framework.inject.Named;
import core.framework.test.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author neo
 */
class OracleIntegrationTest extends IntegrationTest {
    @Inject
    @Named("oracle")
    Database database;

    @Inject
    @Named("oracle")
    Repository<TestSequenceIdDBEntity> repository;

    @BeforeEach
    void truncateTable() {
        database.execute("TRUNCATE TABLE test_sequence_id_entity");
    }

    @Test
    void insert() {
        TestSequenceIdDBEntity entity = new TestSequenceIdDBEntity();
        entity.intField = 1;
        Optional<Long> id = repository.insert(entity);

        assertTrue(id.isPresent());
        TestSequenceIdDBEntity selectedEntity = repository.get(id.get()).get();
        assertEquals(entity.intField, selectedEntity.intField);
    }

    @Test
    void select() {
        for (int i = 0; i < 30; i++) {
            TestSequenceIdDBEntity entity = new TestSequenceIdDBEntity();
            entity.intField = i;
            entity.stringField = "value-" + i;
            repository.insert(entity);
        }

        Query<TestSequenceIdDBEntity> query = repository.select();
        query.where("int_field > ?", 3)
             .where("string_field like ?", "value%")
             .orderBy("int_field")
             .limit(5);

        int count = query.count();
        assertEquals(26, count);

        List<TestSequenceIdDBEntity> entities = query.fetch();
        assertEquals(5, entities.size());
    }
}
