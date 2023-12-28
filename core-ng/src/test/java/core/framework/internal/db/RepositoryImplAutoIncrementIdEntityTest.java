package core.framework.internal.db;

import core.framework.db.Query;
import core.framework.db.Repository;
import core.framework.util.Lists;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RepositoryImplAutoIncrementIdEntityTest {
    private DatabaseImpl database;
    private Repository<AutoIncrementIdEntity> repository;

    @BeforeAll
    void createDatabase() {
        database = new DatabaseImpl("db");
        database.url("jdbc:hsqldb:mem:mysql;sql.syntax_mys=true");
        database.execute("CREATE TABLE auto_increment_id_entity (id INT AUTO_INCREMENT PRIMARY KEY, string_field VARCHAR(20), double_field DOUBLE, enum_field VARCHAR(10), date_time_field DATETIME, zoned_date_time_field TIMESTAMP)");
        repository = database.repository(AutoIncrementIdEntity.class);
    }

    @AfterAll
    void cleanupDatabase() {
        database.execute("DROP TABLE auto_increment_id_entity");
    }

    @BeforeEach
    void truncateTable() {
        database.execute("TRUNCATE TABLE auto_increment_id_entity");
        database.execute("ALTER TABLE auto_increment_id_entity ALTER COLUMN id RESTART WITH 1");
    }

    @Test
    void insert() {
        var entity = new AutoIncrementIdEntity();
        entity.stringField = "string";
        entity.doubleField = 3.25;
        entity.dateTimeField = LocalDateTime.of(2020, 7, 23, 12, 0, 0);
        entity.zonedDateTimeField = ZonedDateTime.of(LocalDateTime.of(2017, Month.APRIL, 3, 12, 0), ZoneId.of("UTC"));

        OptionalLong id = repository.insert(entity);
        assertThat(id).isPresent();

        AutoIncrementIdEntity result = repository.get(id.orElseThrow()).orElseThrow();
        assertThat(result).usingRecursiveComparison()
            .withComparatorForType(ChronoZonedDateTime.timeLineOrder(), ZonedDateTime.class)
            .ignoringFields("id")
            .isEqualTo(entity);
        assertThat(result.id).isEqualTo(id.orElseThrow());
    }

    @Test
    void batchInsert() {
        List<AutoIncrementIdEntity> entities = Lists.newArrayList();
        for (int i = 0; i < 100; i++) {
            AutoIncrementIdEntity entity = entity("string-" + i, 10 + i);
            entities.add(entity);
        }
        Optional<long[]> ids = repository.batchInsert(entities);

        assertThat(ids).isPresent().hasValue(LongStream.range(1, 101).toArray());   // db auto incremental pk starts from 1
        assertThat(repository.get(ids.orElseThrow()[0])).get().usingRecursiveComparison().ignoringFields("id").isEqualTo(entities.get(0));
        assertThat(repository.get(ids.orElseThrow()[1])).get().usingRecursiveComparison().ignoringFields("id").isEqualTo(entities.get(1));
        assertThat(repository.get(ids.orElseThrow()[99])).get().usingRecursiveComparison().ignoringFields("id").isEqualTo(entities.get(99));
    }

    @Test
    void insertIgnore() {
        assertThatThrownBy(() -> repository.insertIgnore(new AutoIncrementIdEntity()))
            .isInstanceOf(Error.class)
            .hasMessageContaining("entity must not have auto increment primary key");
    }

    @Test
    void batchInsertIgnore() {
        assertThatThrownBy(() -> repository.batchInsertIgnore(List.of(new AutoIncrementIdEntity())))
            .isInstanceOf(Error.class)
            .hasMessageContaining("entity must not have auto increment primary key");
    }

    @Test
    void selectOne() {
        var entity = new AutoIncrementIdEntity();
        entity.stringField = "stringField#123456";

        OptionalLong id = repository.insert(entity);
        assertThat(id).isPresent();

        AutoIncrementIdEntity selectedEntity = repository.selectOne("string_field = ?", entity.stringField).orElseThrow();

        assertThat(selectedEntity.id).isEqualTo(id.orElseThrow());
        assertThat(selectedEntity.stringField).isEqualTo(entity.stringField);
    }

    @Test
    void selectAll() {
        Query<AutoIncrementIdEntity> query = repository.select();
        assertThat(query.fetch()).isEmpty();
    }

    @Test
    void selectWithLimit() {
        Query<AutoIncrementIdEntity> query = repository.select();
        assertThatThrownBy(() -> query.limit(0)).isInstanceOf(Error.class).hasMessageContaining("limit must be greater than 0");
    }

    @Test
    void select() {
        var entity1 = new AutoIncrementIdEntity();
        entity1.stringField = "string1";
        entity1.enumField = TestEnum.V1;
        repository.insert(entity1);

        var entity2 = new AutoIncrementIdEntity();
        entity2.stringField = "string2";
        entity2.enumField = TestEnum.V2;
        repository.insert(entity2);

        List<AutoIncrementIdEntity> entities = repository.select("enum_field = ?", TestEnum.V1);
        assertThat(entities).hasSize(1)
            .first().usingRecursiveComparison().ignoringFields("id").isEqualTo(entity1);

        long count = repository.count("enum_field = ?", TestEnum.V2);
        assertThat(count).isEqualTo(1);
    }

    private AutoIncrementIdEntity entity(String stringField, double doubleFiled) {
        var entity = new AutoIncrementIdEntity();
        entity.stringField = stringField;
        entity.doubleField = doubleFiled;
        return entity;
    }
}
