package core.framework.internal.db;

import core.framework.db.Repository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    void get() {
        assertThatThrownBy(() -> repository.get())
            .isInstanceOf(Error.class)
            .hasMessageContaining("the length of primary keys does not match columns");

        assertThatThrownBy(() -> repository.get("id1"))
            .isInstanceOf(Error.class)
            .hasMessageContaining("the length of primary keys does not match columns");
    }

    @Test
    void insert() {
        var entity = new CompositeKeyEntity();
        entity.id1 = "id1";
        entity.id2 = "id2";
        entity.booleanField = Boolean.TRUE;
        entity.longField = 1L;

        repository.insert(entity);

        CompositeKeyEntity selectedEntity = repository.get(entity.id1, entity.id2).orElseThrow();

        assertThat(selectedEntity).usingRecursiveComparison().isEqualTo(entity);
    }

    @Test
    void partialUpdate() {
        var entity = new CompositeKeyEntity();
        entity.id1 = "id1";
        entity.id2 = "id2";
        entity.booleanField = Boolean.TRUE;
        entity.longField = 1L;
        repository.insert(entity);

        entity.longField = 2L;
        boolean result = repository.partialUpdate(entity);
        assertThat(result).isTrue();

        CompositeKeyEntity selectedEntity = repository.get(entity.id1, entity.id2).orElseThrow();
        assertThat(selectedEntity).usingRecursiveComparison().isEqualTo(entity);
    }

    @Test
    void delete() {
        var entity = new CompositeKeyEntity();
        entity.id1 = "id1";
        entity.id2 = "id2";
        entity.booleanField = Boolean.TRUE;
        entity.longField = 1L;

        repository.insert(entity);

        repository.delete(entity.id1, entity.id2);
        assertThat(repository.get(entity.id1, entity.id2)).isNotPresent();

        assertThat(repository.delete("notExistedId1", "notExistedId2")).isFalse();
    }

    @Test
    void deleteWithInvalidPrimaryKeys() {
        assertThatThrownBy(() -> repository.delete("id1"))
            .isInstanceOf(Error.class)
            .hasMessageContaining("the length of primary keys must match columns");
    }

    @Test
    void batchDelete() {
        var entity1 = new CompositeKeyEntity();
        entity1.id1 = "1-1";
        entity1.id2 = "1-2";
        entity1.booleanField = Boolean.TRUE;
        CompositeKeyEntity entity2 = new CompositeKeyEntity();
        entity2.id1 = "2-1";
        entity2.id2 = "2-2";
        entity2.booleanField = Boolean.TRUE;
        repository.batchInsert(List.of(entity1, entity2));

        repository.batchDelete(List.of(new Object[]{entity1.id1, entity1.id2}, new Object[]{entity2.id1, entity2.id2}));

        assertThat(repository.get(entity1.id1, entity1.id2)).isNotPresent();
        assertThat(repository.get(entity2.id1, entity2.id2)).isNotPresent();
    }

    @Test
    void batchDeleteParams() {
        assertThatThrownBy(() -> {
            var impl = (RepositoryImpl<CompositeKeyEntity>) repository;
            impl.batchDeleteParams(List.of(1, 2, 3));
        }).isInstanceOf(Error.class)
            .hasMessageContaining("the length of primary keys must match columns");

        assertThatThrownBy(() -> {
            var impl = (RepositoryImpl<CompositeKeyEntity>) repository;
            impl.batchDeleteParams(List.of(new Object[]{1, 1, 1}));
        }).isInstanceOf(Error.class)
            .hasMessageContaining("the length of primary keys must match columns");
    }
}
