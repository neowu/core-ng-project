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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        AutoIncrementIdEntity entity = new AutoIncrementIdEntity();
        entity.stringField = "string";
        entity.doubleField = 3.25;
        entity.dateTimeField = LocalDateTime.now();
        entity.zonedDateTimeField = ZonedDateTime.of(LocalDateTime.of(2017, Month.APRIL, 3, 12, 0), ZoneId.of("UTC"));

        Optional<Long> id = repository.insert(entity);
        assertTrue(id.isPresent());

        AutoIncrementIdEntity selectedEntity = repository.get(id.get()).orElseThrow(() -> new Error("not found"));

        assertEquals((long) id.get(), (long) selectedEntity.id);
        assertEquals(entity.stringField, selectedEntity.stringField);
        assertEquals(entity.doubleField, selectedEntity.doubleField);
        assertEquals(entity.dateTimeField, selectedEntity.dateTimeField);
        assertEquals(entity.zonedDateTimeField.toInstant(), selectedEntity.zonedDateTimeField.toInstant());
    }

    @Test
    void selectOne() {
        AutoIncrementIdEntity entity = new AutoIncrementIdEntity();
        entity.stringField = "stringField#123456";

        Optional<Long> id = repository.insert(entity);
        assertTrue(id.isPresent());

        AutoIncrementIdEntity selectedEntity = repository.selectOne("string_field = ?", entity.stringField).orElseThrow(() -> new Error("not found"));

        assertEquals((long) id.get(), (long) selectedEntity.id);
        assertEquals(entity.stringField, selectedEntity.stringField);
    }

    @Test
    void selectAll() {
        List<AutoIncrementIdEntity> entities = repository.select().fetch();
        assertTrue(entities.isEmpty());
    }

    @Test
    void selectWithLimit() {
        Query<AutoIncrementIdEntity> query = repository.select().limit(1000);
        List<AutoIncrementIdEntity> entities = query.fetch();
        assertTrue(entities.isEmpty());

        query.where("string_field = ?", "value");
        entities = query.fetch();
        assertTrue(entities.isEmpty());
    }

    @Test
    void select() {
        AutoIncrementIdEntity entity1 = new AutoIncrementIdEntity();
        entity1.stringField = "string1";
        entity1.enumField = TestEnum.V1;
        repository.insert(entity1);

        AutoIncrementIdEntity entity2 = new AutoIncrementIdEntity();
        entity2.stringField = "string2";
        entity2.enumField = TestEnum.V2;
        repository.insert(entity2);

        List<AutoIncrementIdEntity> entities = repository.select("enum_field = ?", TestEnum.V1);

        assertEquals(1, entities.size());
        assertEquals(entity1.enumField, entities.get(0).enumField);
        assertEquals(entity1.stringField, entities.get(0).stringField);
    }
}
