package core.framework.internal.db;

import core.framework.db.IsolationLevel;
import core.framework.db.Query;
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
import java.util.Optional;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

        assertThat(repository.get(entity.id)).get().usingRecursiveComparison().isEqualTo(entity);
    }

    @Test
    void insertIgnore() {
        String id = UUID.randomUUID().toString();
        AssignedIdEntity entity = entity(id, "string", 12);

        boolean inserted = repository.insertIgnore(entity);
        assertThat(inserted).isTrue();

        inserted = repository.insertIgnore(entity);
        assertThat(inserted).isFalse();
    }

    @Test
    void validateId() {
        AssignedIdEntity entity = entity(null, "string", 1);

        assertThatThrownBy(() -> repository.insert(entity))
                .isInstanceOf(Error.class)
                .hasMessageContaining("primary key must not be null");
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

        AssignedIdEntity result = repository.get(entity.id).orElseThrow();
        assertThat(result).usingRecursiveComparison().isEqualTo(updatedEntity);
        assertThat(result.stringField).isNull();
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

        AssignedIdEntity result = repository.get(entity.id).orElseThrow();
        assertThat(result.stringField).isEqualTo(updatedEntity.stringField);
        assertThat(result.dateField).isEqualTo(updatedEntity.dateField);
        assertThat(result.intField).isEqualTo(11);
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

        assertThat(repository.get("1")).get().usingRecursiveComparison().isEqualTo(entities.get(0));
        assertThat(repository.get("2")).get().usingRecursiveComparison().isEqualTo(entities.get(1));
    }

    @Test
    void batchInsertWithEmptyEntities() {
        assertThatThrownBy(() -> repository.batchInsert(List.of()))
                .isInstanceOf(Error.class)
                .hasMessageContaining("entities must not be empty");
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


    @Test
    void batchDeleteWithEmptyPrimaryKeys() {
        assertThatThrownBy(() -> repository.batchDelete(List.of()))
                .isInstanceOf(Error.class)
                .hasMessageContaining("primaryKeys must not be empty");
    }

    @Test
    void selectWithGroupBy() {
        List<AssignedIdEntity> entities = Lists.newArrayList();
        for (int i = 200; i < 250; i++) {
            AssignedIdEntity entity = entity(String.valueOf(i), "group", 1);
            entities.add(entity);
        }
        repository.batchInsert(entities);

        Query<AssignedIdEntity> query = repository.select();
        query.where("string_field = ?", "group");
        query.groupBy("string_field");
        Optional<Integer> sum = query.project("sum(int_field)", Integer.class);

        assertThat(sum).hasValue(50);

        assertThatThrownBy(query::fetch)
                .isInstanceOf(Error.class)
                .hasMessageContaining("fetch must not be used with groupBy");
    }

    @Test
    void select() {
        List<AssignedIdEntity> entities = Lists.newArrayList();
        for (int i = 300; i < 350; i++) {
            AssignedIdEntity entity = entity(String.valueOf(i), "value" + i, i);
            entities.add(entity);
        }
        repository.batchInsert(entities);

        Query<AssignedIdEntity> query = repository.select();
        query.orderBy("int_field");
        query.skip(5);
        query.limit(5);

        List<AssignedIdEntity> results = query.fetch();
        assertThat(results).hasSize(5);
        assertThat(results.get(0).intField).isEqualTo(305);
        assertThat(results.get(4).intField).isEqualTo(309);

        query.where("string_field like ?", "value32%");
        query.skip(0);
        query.limit(100);

        results = query.fetch();
        assertThat(results).hasSize(10);
        assertThat(results.get(0).intField).isEqualTo(320);
        assertThat(results.get(4).intField).isEqualTo(324);
    }

    @Test
    void count() {
        List<AssignedIdEntity> entities = Lists.newArrayList();
        for (int i = 400; i < 450; i++) {
            AssignedIdEntity entity = entity(String.valueOf(i), "value" + i, i);
            entities.add(entity);
        }
        repository.batchInsert(entities);

        Query<AssignedIdEntity> query = repository.select();
        query.orderBy("int_field");
        assertThat(query.count()).isEqualTo(50);

        query.where("string_field like ?", "value42%");
        assertThat(query.count()).isEqualTo(10);
    }

    private AssignedIdEntity entity(String id, String stringField, int intField) {
        var entity = new AssignedIdEntity();
        entity.id = id;
        entity.stringField = stringField;
        entity.intField = intField;
        return entity;
    }
}
