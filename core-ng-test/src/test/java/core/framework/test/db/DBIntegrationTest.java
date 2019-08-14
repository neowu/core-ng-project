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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class DBIntegrationTest extends IntegrationTest {
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

        Optional<TestDBEntity> selectedEntity = repository.get(entity.id);
        assertThat(selectedEntity).get().isEqualToComparingFieldByField(entity);
    }

    @Test
    void select() {
        createTestEntities();

        Query<TestDBEntity> query = repository.select();
        query.where("int_field > ?", 3);
        query.where("string_field like ?", "value%");
        query.orderBy("int_field");
        assertThat(query.count()).isEqualTo(26);

        Optional<Integer> sum = query.project("sum(int_field)", Integer.class);
        assertThat(sum).hasValue(429);  // (4+29)*(26/2)

        query.limit(5);
        List<TestDBEntity> entities = query.fetch();
        assertThat(entities).hasSize(5);
        assertThat(entities.get(0).intField).isEqualTo(4);
    }

    @Test
    void selectWithWhere() {
        createTestEntities();

        List<TestDBEntity> entities = repository.select("string_field = ?", "value-10");
        assertThat(entities).hasSize(1);
        assertThat(entities.get(0).intField).isEqualTo(10);
    }

    @Test
    void selectOne() {
        var entity = new TestDBEntity();
        entity.id = UUID.randomUUID().toString();
        entity.intField = 2;
        entity.stringField = "value-2";
        repository.insert(entity);

        Query<TestDBEntity> query = repository.select();
        query.where("int_field = ?", 2);
        Optional<TestDBEntity> result = query.fetchOne();

        assertThat(result).isPresent().get().isEqualToComparingFieldByField(entity);
    }

    private void createTestEntities() {
        for (int i = 0; i < 30; i++) {
            var entity = new TestDBEntity();
            entity.id = UUID.randomUUID().toString();
            entity.intField = i;
            entity.stringField = "value-" + i;
            repository.insert(entity);
        }
    }
}
