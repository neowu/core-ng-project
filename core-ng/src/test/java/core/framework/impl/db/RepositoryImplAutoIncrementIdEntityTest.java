package core.framework.impl.db;

import core.framework.db.Query;
import core.framework.db.Repository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.OptionalLong;

import static org.assertj.core.api.Assertions.assertThat;

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
        database.vendor = Vendor.MYSQL;

        database.execute("CREATE TABLE auto_increment_id_entity (id INT AUTO_INCREMENT PRIMARY KEY, string_field VARCHAR(20), double_field DOUBLE, enum_field VARCHAR(10), date_time_field TIMESTAMP, zoned_date_time_field TIMESTAMP)");

        repository = database.repository(AutoIncrementIdEntity.class);
    }

    @AfterAll
    void cleanupDatabase() {
        database.execute("DROP TABLE auto_increment_id_entity");
    }

    @BeforeEach
    void truncateTable() {
        database.execute("TRUNCATE TABLE auto_increment_id_entity");
    }

    @Test
    void insert() {
        var entity = new AutoIncrementIdEntity();
        entity.stringField = "string";
        entity.doubleField = 3.25;
        entity.dateTimeField = LocalDateTime.now();
        entity.zonedDateTimeField = ZonedDateTime.of(LocalDateTime.of(2017, Month.APRIL, 3, 12, 0), ZoneId.of("UTC"));

        OptionalLong id = repository.insert(entity);
        assertThat(id).isPresent();

        assertThat(repository.get(id.orElseThrow()))
                .get().isEqualToIgnoringGivenFields(entity, "id", "zonedDateTimeField")
                .satisfies(selectedEntity -> {
                    assertThat(selectedEntity.id).isEqualTo(id.orElseThrow());
                    assertThat(selectedEntity.zonedDateTimeField).isEqualTo(entity.zonedDateTimeField);
                });
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
        query.limit(0);
        assertThat(query.fetch()).isEmpty();
        assertThat(query.fetchOne()).isEmpty();

        query.limit(1000);
        assertThat(query.fetch()).isEmpty();

        query.where("string_field = ?", "value");
        assertThat(query.fetch()).isEmpty();
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
        assertThat(entities).hasSize(1);
        assertThat(entities.get(0)).isEqualToIgnoringGivenFields(entity1, "id");

        int count = repository.count("enum_field = ?", TestEnum.V1);
        assertThat(count).isEqualTo(1);
    }
}
