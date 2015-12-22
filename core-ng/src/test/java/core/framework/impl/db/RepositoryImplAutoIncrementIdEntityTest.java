package core.framework.impl.db;

import core.framework.api.db.Query;
import core.framework.api.db.Repository;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * @author neo
 */
public class RepositoryImplAutoIncrementIdEntityTest {
    private static DatabaseImpl database;
    private static Repository<AutoIncrementIdEntity> repository;

    @BeforeClass
    public static void createDatabase() {
        database = new DatabaseImpl();
        database.url("jdbc:hsqldb:mem:.;sql.syntax_mys=true");
        database.execute("CREATE TABLE auto_increment_id_entity (id INT AUTO_INCREMENT PRIMARY KEY, string_field VARCHAR(20), double_field DOUBLE, enum_field VARCHAR(10), date_time_field TIMESTAMP)");

        repository = database.repository(AutoIncrementIdEntity.class);
    }

    @AfterClass
    public static void cleanupDatabase() {
        database.execute("DROP TABLE auto_increment_id_entity");
    }

    @Before
    public void truncateTable() {
        database.execute("TRUNCATE TABLE auto_increment_id_entity");
    }

    @Test
    public void insert() {
        AutoIncrementIdEntity entity = new AutoIncrementIdEntity();
        entity.stringField = "string";
        entity.doubleField = 3.25;
        entity.dateTimeField = LocalDateTime.now();

        Optional<Long> id = repository.insert(entity);
        Assert.assertTrue(id.isPresent());

        AutoIncrementIdEntity selectedEntity = repository.get(id.get()).get();

        Assert.assertEquals((long) id.get(), (long) selectedEntity.id);
        Assert.assertEquals(entity.stringField, selectedEntity.stringField);
        Assert.assertEquals(entity.doubleField, selectedEntity.doubleField);
        Assert.assertEquals(entity.dateTimeField, selectedEntity.dateTimeField);
    }

    @Test
    public void selectOne() {
        AutoIncrementIdEntity entity = new AutoIncrementIdEntity();
        entity.stringField = "stringField#123456";

        Optional<Long> id = repository.insert(entity);
        Assert.assertTrue(id.isPresent());

        AutoIncrementIdEntity selectedEntity = repository.selectOne("string_field = ?", entity.stringField).get();

        Assert.assertEquals((long) id.get(), (long) selectedEntity.id);
        Assert.assertEquals(entity.stringField, selectedEntity.stringField);
    }

    @Test
    public void selectAll() {
        List<AutoIncrementIdEntity> entities = repository.select(new Query());
        Assert.assertTrue(entities.isEmpty());
    }

    @Test
    public void selectWithLimit() {
        Query query = new Query();
        query.skip = 0;
        query.limit = 1000;
        List<AutoIncrementIdEntity> entities = repository.select(query);
        Assert.assertTrue(entities.isEmpty());

        query.where = "string_field = ?";
        query.params = new Object[]{"value"};
        entities = repository.select(query);
        Assert.assertTrue(entities.isEmpty());
    }

    @Test
    public void select() {
        AutoIncrementIdEntity entity1 = new AutoIncrementIdEntity();
        entity1.stringField = "string1";
        entity1.enumField = TestEnum.V1;
        repository.insert(entity1);

        AutoIncrementIdEntity entity2 = new AutoIncrementIdEntity();
        entity2.stringField = "string2";
        entity2.enumField = TestEnum.V2;
        repository.insert(entity2);

        List<AutoIncrementIdEntity> entities = repository.select("enum_field = ?", TestEnum.V1);

        Assert.assertEquals(1, entities.size());
        Assert.assertEquals(entity1.enumField, entities.get(0).enumField);
        Assert.assertEquals(entity1.stringField, entities.get(0).stringField);
    }
}