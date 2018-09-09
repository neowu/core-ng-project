package core.framework.impl.db;

import core.framework.db.Repository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author neo
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RepositoryImplCompositeKeyEntityTest {
    private DatabaseImpl database;
    private Repository<CompositeKeyEntity> repository;

    @BeforeAll
    void createDatabase() {
        database = new DatabaseImpl("db");
        database.url("jdbc:hsqldb:mem:.;sql.syntax_mys=true");
        database.vendor = Vendor.MYSQL;
        database.execute("CREATE TABLE composite_key_entity (id1 VARCHAR(36), id2 VARCHAR(36), boolean_field BIT(1), long_field BIGINT, PRIMARY KEY (id1, id2))");

        repository = database.repository(CompositeKeyEntity.class);
    }

    @AfterAll
    void cleanupDatabase() {
        database.execute("DROP TABLE composite_key_entity");
    }

    @BeforeEach
    void truncateTable() {
        database.execute("TRUNCATE TABLE composite_key_entity");
    }

    @Test
    void insert() {
        CompositeKeyEntity entity = new CompositeKeyEntity();
        entity.id1 = "id1";
        entity.id2 = "id2";
        entity.booleanField = Boolean.TRUE;
        entity.longField = 1L;

        repository.insert(entity);

        CompositeKeyEntity selectedEntity = repository.get(entity.id1, entity.id2).orElseThrow();

        assertEquals(entity.id1, selectedEntity.id1);
        assertEquals(entity.id2, selectedEntity.id2);
        assertEquals(entity.booleanField, selectedEntity.booleanField);
        assertEquals(entity.longField, selectedEntity.longField);
    }

    @Test
    void partialUpdate() {
        CompositeKeyEntity entity = new CompositeKeyEntity();
        entity.id1 = "id1";
        entity.id2 = "id2";
        entity.booleanField = Boolean.TRUE;
        entity.longField = 1L;
        repository.insert(entity);

        entity.longField = 2L;
        repository.partialUpdate(entity);

        CompositeKeyEntity selectedEntity = repository.get(entity.id1, entity.id2).orElseThrow();
        assertEquals(entity.longField, selectedEntity.longField);
    }

    @Test
    void batchDelete() {
        CompositeKeyEntity entity1 = new CompositeKeyEntity();
        entity1.id1 = "1-1";
        entity1.id2 = "1-2";
        entity1.booleanField = Boolean.TRUE;
        CompositeKeyEntity entity2 = new CompositeKeyEntity();
        entity2.id1 = "2-1";
        entity2.id2 = "2-2";
        entity2.booleanField = Boolean.TRUE;
        repository.batchInsert(List.of(entity1, entity2));

        repository.batchDelete(List.of(new Object[]{entity1.id1, entity1.id2}, new Object[]{entity2.id1, entity2.id2}));

        assertFalse(repository.get(entity1.id1, entity1.id2).isPresent());
        assertFalse(repository.get(entity2.id1, entity2.id2).isPresent());
    }
}
