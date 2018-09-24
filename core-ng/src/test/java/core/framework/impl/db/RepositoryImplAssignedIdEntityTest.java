package core.framework.impl.db;

import core.framework.db.IsolationLevel;
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
import java.util.List;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
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
        database.isolationLevel = IsolationLevel.READ_UNCOMMITTED;
        database.operation.batchSize = 7;
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
        AssignedIdEntity entity = entity(UUID.randomUUID().toString(), "string", 12);
        entity.bigDecimalField = new BigDecimal("86.99");
        entity.dateField = LocalDate.of(2016, Month.JULY, 5);

        OptionalLong id = repository.insert(entity);
        assertFalse(id.isPresent());

        assertThat(repository.get(entity.id)).get().isEqualToComparingFieldByField(entity);
    }

    @Test
    void update() {
        AssignedIdEntity entity = entity(UUID.randomUUID().toString(), "string", 11);
        repository.insert(entity);

        AssignedIdEntity updatedEntity = new AssignedIdEntity();
        updatedEntity.id = entity.id;
        updatedEntity.dateField = LocalDate.of(2016, Month.JULY, 5);
        updatedEntity.intField = 12;
        repository.update(updatedEntity);

        assertThat(repository.get(entity.id))
                .get().isEqualToComparingFieldByField(updatedEntity)
                .satisfies(selectedEntity -> assertThat(selectedEntity.stringField).isNull());
    }

    @Test
    void partialUpdate() {
        AssignedIdEntity entity = entity(UUID.randomUUID().toString(), "string", 11);
        repository.insert(entity);

        AssignedIdEntity updatedEntity = new AssignedIdEntity();
        updatedEntity.id = entity.id;
        updatedEntity.stringField = "updated";
        updatedEntity.dateField = LocalDate.of(2016, Month.JULY, 5);
        repository.partialUpdate(updatedEntity);

        assertThat(repository.get(entity.id))
                .get().isEqualToComparingOnlyGivenFields(updatedEntity, "stringField", "dateField")
                .satisfies(selectedEntity -> assertThat(selectedEntity.intField).isEqualTo(11));
    }

    @Test
    void delete() {
        AssignedIdEntity entity = entity(UUID.randomUUID().toString(), "string", 11);
        repository.insert(entity);

        repository.delete(entity.id);

        assertThat(repository.get(entity.id)).isNotPresent();
    }

    @Test
    void batchInsert() {
        List<AssignedIdEntity> entities = Lists.newArrayList();
        for (int i = 1; i < 100; i++) {
            AssignedIdEntity entity = entity(String.valueOf(i), "value" + i, 10 + i);
            entities.add(entity);
        }
        repository.batchInsert(entities);

        assertThat(repository.get("1")).get().isEqualToComparingFieldByFieldRecursively(entities.get(0));
        assertThat(repository.get("2")).get().isEqualToComparingFieldByFieldRecursively(entities.get(1));
    }

    @Test
    void batchDelete() {
        List<AssignedIdEntity> entities = Lists.newArrayList();
        for (int i = 100; i < 200; i++) {
            AssignedIdEntity entity = entity(String.valueOf(i), "value" + i, 10 + i);
            entities.add(entity);
        }
        repository.batchInsert(entities);

        repository.batchDelete(entities.stream().map(entity -> entity.id).collect(Collectors.toList()));

        assertThat(repository.get(entities.get(0).id)).isNotPresent();
        assertThat(repository.get(entities.get(1).id)).isNotPresent();
    }

    private AssignedIdEntity entity(String id, String stringField, int intField) {
        var entity = new AssignedIdEntity();
        entity.id = id;
        entity.stringField = stringField;
        entity.intField = intField;
        return entity;
    }
}
