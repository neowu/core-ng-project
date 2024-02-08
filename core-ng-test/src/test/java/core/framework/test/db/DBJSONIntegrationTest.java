package core.framework.test.db;

import core.framework.db.Database;
import core.framework.db.Repository;
import core.framework.inject.Inject;
import core.framework.internal.validate.ValidationException;
import core.framework.test.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class DBJSONIntegrationTest extends IntegrationTest {
    @Inject
    Database database;
    @Inject
    Repository<TestDBEntityWithJSON> repository;

    @BeforeEach
    void truncateTable() {
        database.execute("TRUNCATE TABLE test_entity_with_json");
    }

    @Test
    void insert() {
        var entity = new TestDBEntityWithJSON();
        entity.id = UUID.randomUUID().toString();
        entity.enumList = List.of(TestDBEntityWithJSON.TestEnum.VALUE1);
        entity.intList = List.of(1, 2, 3, 4, 5);
        entity.jsonField = new TestDBEntityWithJSON.TestJSON();
        entity.jsonField.data = "test";
        repository.insert(entity);

        Optional<TestDBEntityWithJSON> selectedEntity = repository.get(entity.id);
        assertThat(selectedEntity).get()
            .usingRecursiveComparison().isEqualTo(entity);
    }

    @Test
    void update() {
        var entity = new TestDBEntityWithJSON();
        entity.id = UUID.randomUUID().toString();
        repository.insert(entity);
        Optional<TestDBEntityWithJSON> selectedEntity = repository.get(entity.id);
        assertThat(selectedEntity).get()
            .usingRecursiveComparison().isEqualTo(entity);

        entity.enumList = List.of(TestDBEntityWithJSON.TestEnum.VALUE2);
        entity.intList = List.of(1, 2, 3, 4, 5);
        entity.jsonField = new TestDBEntityWithJSON.TestJSON();
        entity.jsonField.data = "test";
        repository.update(entity);
        selectedEntity = repository.get(entity.id);
        assertThat(selectedEntity).get()
            .usingRecursiveComparison().isEqualTo(entity);
    }

    @Test
    void validate() {
        var entity = new TestDBEntityWithJSON();
        entity.id = UUID.randomUUID().toString();
        entity.jsonField = new TestDBEntityWithJSON.TestJSON();
        assertThatThrownBy(() -> repository.insert(entity))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("jsonField.data=field must not be null");
    }
}
