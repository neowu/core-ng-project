package core.framework.impl.db;

import core.framework.db.Query;
import core.framework.db.Repository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author neo
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RepositoryImplSequenceIdEntityTest {
    private DatabaseImpl database;
    private Repository<SequenceIdEntity> repository;

    @BeforeAll
    void createDatabase() {
        database = new DatabaseImpl("db");
        database.url("jdbc:hsqldb:mem:seq;sql.syntax_ora=true");
        database.vendor = Vendor.ORACLE;
        database.execute("CREATE TABLE sequence_id_entity (id VARCHAR(36) PRIMARY KEY, string_field VARCHAR(20), long_field BIGINT)");
        database.execute("CREATE SEQUENCE seq");

        repository = database.repository(SequenceIdEntity.class);
    }

    @AfterAll
    void cleanupDatabase() {
        database.execute("DROP TABLE sequence_id_entity");
        database.execute("DROP SEQUENCE seq");
    }

    @BeforeEach
    void truncateTable() {
        database.execute("TRUNCATE TABLE sequence_id_entity");
    }

    @Test
    void insert() {
        SequenceIdEntity entity = new SequenceIdEntity();
        entity.stringField = "string";

        Optional<Long> id = repository.insert(entity);
        assertTrue(id.isPresent());

        SequenceIdEntity selectedEntity = repository.get(id.get()).get();

        assertEquals((long) id.get(), (long) selectedEntity.id);
        assertEquals(entity.stringField, selectedEntity.stringField);
    }

    @Test
    void count() {
        createEntities();

        Query<SequenceIdEntity> query = repository.select();
        assertEquals(30, query.count());

        query.where("string_field like ?", "value2%");
        assertEquals(11, query.count());
    }

    @Test
    void select() {
        createEntities();

        Query<SequenceIdEntity> query = repository.select();
        query.orderBy("long_field");
        query.limit(5);

        int count = query.count();
        assertEquals(30, count);

        query.skip(0);
        List<SequenceIdEntity> entities = query.fetch();
        assertEquals(5, entities.size());
        assertEquals("value1", entities.get(0).stringField);
        assertEquals(Long.valueOf(1), entities.get(0).longField);
        assertEquals(Long.valueOf(5), entities.get(4).longField);

        query.skip(5);
        entities = query.fetch();
        assertEquals(5, entities.size());
        assertEquals(Long.valueOf(6), entities.get(0).longField);
        assertEquals(Long.valueOf(10), entities.get(4).longField);

        query.skip(10);
        entities = query.fetch();
        assertEquals(5, entities.size());
        assertEquals(Long.valueOf(11), entities.get(0).longField);
        assertEquals(Long.valueOf(15), entities.get(4).longField);

        query.where("long_field > ?", 10);
        query.skip(0);
        entities = query.fetch();
        assertEquals(5, entities.size());
        assertEquals("value11", entities.get(0).stringField);
        assertEquals(Long.valueOf(11), entities.get(0).longField);
        assertEquals(Long.valueOf(15), entities.get(4).longField);

        query.skip(5);
        entities = query.fetch();
        assertEquals(5, entities.size());
        assertEquals(Long.valueOf(16), entities.get(0).longField);
        assertEquals(Long.valueOf(20), entities.get(4).longField);
    }

    private void createEntities() {
        for (int i = 1; i <= 30; i++) {
            SequenceIdEntity entity = new SequenceIdEntity();
            entity.stringField = "value" + i;
            entity.longField = (long) i;
            repository.insert(entity);
        }
    }
}
