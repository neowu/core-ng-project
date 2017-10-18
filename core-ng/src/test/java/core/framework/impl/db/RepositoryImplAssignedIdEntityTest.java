package core.framework.impl.db;

import core.framework.db.Repository;
import core.framework.util.Lists;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author neo
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RepositoryImplAssignedIdEntityTest {
    private DatabaseImpl database;
    private Repository<AssignedIdEntity> repository;

    @BeforeAll
    void createDatabase() {
        database = new DatabaseImpl("db");
        database.url("jdbc:hsqldb:mem:.;sql.syntax_mys=true");
        database.vendor = Vendor.MYSQL;
        database.execute("CREATE TABLE assigned_id_entity (id VARCHAR(36) PRIMARY KEY, string_field VARCHAR(20), int_field INT, big_decimal_field DECIMAL(10,2), date_field DATE)");

        repository = database.repository(AssignedIdEntity.class);
    }

    @AfterAll
    void cleanupDatabase() {
        database.execute("DROP TABLE assigned_id_entity");
    }

    @BeforeEach
    void truncateTable() {
        database.execute("TRUNCATE TABLE assigned_id_entity");
    }

    @Test
    void insert() {
        AssignedIdEntity entity = new AssignedIdEntity();
        entity.id = UUID.randomUUID().toString();
        entity.stringField = "string";
        entity.intField = 12;
        entity.bigDecimalField = new BigDecimal("86.99");
        entity.dateField = LocalDate.of(2016, Month.JULY, 5);

        Optional<Long> id = repository.insert(entity);
        assertFalse(id.isPresent());

        AssignedIdEntity selectedEntity = repository.get(entity.id).get();

        assertEquals(entity.id, selectedEntity.id);
        assertEquals(entity.stringField, selectedEntity.stringField);
        assertEquals(entity.intField, selectedEntity.intField);
        assertEquals(entity.bigDecimalField, selectedEntity.bigDecimalField);
        assertEquals(entity.dateField, selectedEntity.dateField);
    }

    @Test
    void update() {
        AssignedIdEntity entity = new AssignedIdEntity();
        entity.id = UUID.randomUUID().toString();
        entity.stringField = "string";
        entity.intField = 11;
        repository.insert(entity);

        AssignedIdEntity updatedEntity = new AssignedIdEntity();
        updatedEntity.id = entity.id;
        updatedEntity.stringField = "updated";
        updatedEntity.dateField = LocalDate.of(2016, Month.JULY, 5);
        repository.update(updatedEntity);

        AssignedIdEntity selectedEntity = repository.get(entity.id).get();
        assertEquals(updatedEntity.stringField, selectedEntity.stringField);
        assertEquals(entity.intField, selectedEntity.intField);
        assertEquals(updatedEntity.dateField, selectedEntity.dateField);
    }

    @Test
    void delete() {
        AssignedIdEntity entity = new AssignedIdEntity();
        entity.id = UUID.randomUUID().toString();
        entity.intField = 11;
        repository.insert(entity);

        repository.delete(entity.id);

        Optional<AssignedIdEntity> result = repository.get(entity.id);
        assertFalse(result.isPresent());
    }

    @Test
    void batchInsert() {
        AssignedIdEntity entity1 = new AssignedIdEntity();
        entity1.id = "1";
        entity1.stringField = "value1";
        entity1.intField = 11;

        AssignedIdEntity entity2 = new AssignedIdEntity();
        entity2.id = "2";
        entity2.stringField = "value2";
        entity2.intField = 12;

        repository.batchInsert(Lists.newArrayList(entity1, entity2));

        AssignedIdEntity selectedEntity1 = repository.get("1").get();
        assertEquals(entity1.stringField, selectedEntity1.stringField);

        AssignedIdEntity selectedEntity2 = repository.get("2").get();
        assertEquals(entity2.stringField, selectedEntity2.stringField);
    }

    @Test
    void batchDelete() {
        AssignedIdEntity entity1 = new AssignedIdEntity();
        entity1.id = "3";
        entity1.intField = 11;
        AssignedIdEntity entity2 = new AssignedIdEntity();
        entity2.id = "4";
        entity2.intField = 12;
        repository.batchInsert(Lists.newArrayList(entity1, entity2));

        repository.batchDelete(Lists.newArrayList(entity1.id, entity2.id));

        assertFalse(repository.get(entity1.id).isPresent());
        assertFalse(repository.get(entity2.id).isPresent());
    }
}
