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

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(id).isPresent();

        Optional<TestSequenceIdDBEntity> selectedEntity = repository.get(id.get());
        assertThat(selectedEntity).get().isEqualToIgnoringGivenFields(entity, "id");
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
        query.where("int_field > ?", 3);
        query.where("string_field like ?", "value%");
        query.orderBy("int_field");
        query.limit(5);

        int count = query.count();
        assertThat(count).isEqualTo(26);

        List<TestSequenceIdDBEntity> entities = query.fetch();
        assertThat(entities).hasSize(5);
    }

    @Test
    void selectOne() {
        TestSequenceIdDBEntity entity1 = new TestSequenceIdDBEntity();
        entity1.intField = 1;
        repository.insert(entity1);
        TestSequenceIdDBEntity entity2 = new TestSequenceIdDBEntity();
        entity2.intField = 2;
        repository.insert(entity2);

        Query<TestSequenceIdDBEntity> query = repository.select();
        query.where("int_field = ?", 2);
        Optional<TestSequenceIdDBEntity> result = query.fetchOne();

        assertThat(result).isPresent().get().isEqualToIgnoringGivenFields(entity2, "id");
    }
}
