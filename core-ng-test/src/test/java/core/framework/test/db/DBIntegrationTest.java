package core.framework.test.db;

import core.framework.db.Database;
import core.framework.db.Query;
import core.framework.db.Repository;
import core.framework.inject.Inject;
import core.framework.test.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
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
        var entity = new TestDBEntity();
        entity.id = UUID.randomUUID().toString();
        entity.dateTimeField = LocalDateTime.of(2020, Month.JULY, 23, 14, 0, 0);
        entity.dateField = entity.dateTimeField.toLocalDate();
        entity.zonedDateTimeField = ZonedDateTime.of(entity.dateTimeField, ZoneId.systemDefault());
        repository.insert(entity);

        Optional<TestDBEntity> selectedEntity = repository.get(entity.id);
        assertThat(selectedEntity).get()
            .usingRecursiveComparison().isEqualTo(entity);
    }

    @Test
    void select() {
        createTestEntities();

        Query<TestDBEntity> query = repository.select();
        query.where("int_field > ?", 3);
        query.where("string_field like ?", "value%");
        assertThat(query.count()).isEqualTo(26);

        Optional<Integer> sum = query.projectOne("sum(int_field)", Integer.class);
        assertThat(sum).hasValue(429);  // (4+29)*(26/2)

        query.orderBy("int_field");
        query.limit(5);
        List<TestDBEntity> entities = query.fetch();
        assertThat(entities).hasSize(5);
        assertThat(entities.get(0).intField).isEqualTo(4);

        // with or condition
        query = repository.select();
        query.where("int_field >= ? OR int_field <= ?", 2, 4);
        query.where("string_field = ?", "value-3");
        assertThat(query.count()).isEqualTo(1);

        entities = query.fetch();
        assertThat(entities).hasSize(1);
        assertThat(entities.get(0).intField).isEqualTo(3);
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

        assertThat(result).get()
            .usingRecursiveComparison().isEqualTo(entity);

        TestDBView view = query.projectOne("id, int_field", TestDBView.class).orElseThrow();
        assertThat(view.id).isEqualTo(entity.id);
        assertThat(view.intField).isEqualTo(entity.intField);
    }

    @Test
    void projection() {
        createTestEntities();
        createTestEntities();

        Query<TestDBEntity> query = repository.select();
        query.groupBy("string_field");
        query.orderBy("string_field DESC");
        query.limit(5);
        List<TestDBProjection> projections = query.project("string_field, sum(int_field) as sum_value", TestDBProjection.class);

        assertThat(projections).hasSize(5);
        assertThat(projections.get(0).stringField).isEqualTo("value-9");
        assertThat(projections.get(0).sum).isEqualTo(18);

        query.where("string_field = ?", "value-8");
        // projectOne ignores sort, skip and limit
        TestDBProjection projection = query.projectOne("string_field, sum(int_field) as sum_value", TestDBProjection.class).orElseThrow();
        assertThat(projection.stringField).isEqualTo("value-8");
        assertThat(projection.sum).isEqualTo(16);

        // SELECT count(1) FROM test_entity WHERE string_field = ? GROUP BY string_field
        assertThat(query.count()).isEqualTo(2);
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
