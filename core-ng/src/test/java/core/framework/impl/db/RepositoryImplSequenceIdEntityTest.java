package core.framework.impl.db;

import core.framework.api.db.Query;
import core.framework.api.db.Repository;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

/**
 * @author neo
 */
public class RepositoryImplSequenceIdEntityTest {
    private static DatabaseImpl database;
    private static Repository<SequenceIdEntity> repository;

    @BeforeClass
    public static void createDatabase() {
        database = new DatabaseImpl();
        database.url("jdbc:hsqldb:mem:seq;sql.syntax_ora=true");
        database.vendor = Vendor.ORACLE;
        database.execute("CREATE TABLE sequence_id_entity (id VARCHAR(36) PRIMARY KEY, string_field VARCHAR(20))");
        database.execute("CREATE SEQUENCE seq");

        repository = database.repository(SequenceIdEntity.class);
    }

    @AfterClass
    public static void cleanupDatabase() {
        database.execute("DROP TABLE sequence_id_entity");
        database.execute("DROP SEQUENCE seq");
    }

    @Before
    public void truncateTable() {
        database.execute("TRUNCATE TABLE sequence_id_entity");
    }

    @Test
    public void insert() {
        SequenceIdEntity entity = new SequenceIdEntity();
        entity.stringField = "string";

        Optional<Long> id = repository.insert(entity);
        Assert.assertTrue(id.isPresent());

        SequenceIdEntity selectedEntity = repository.get(id.get()).get();

        assertEquals((long) id.get(), (long) selectedEntity.id);
        assertEquals(entity.stringField, selectedEntity.stringField);
    }

    @Test
    public void select() {
        for (int i = 0; i < 30; i++) {
            SequenceIdEntity entity = new SequenceIdEntity();
            entity.stringField = "value" + i;
            repository.insert(entity);
        }

        Query<SequenceIdEntity> query = repository.select();
        assertEquals(30, query.count());

        query.where("string_field like ?", "value2%");
        assertEquals(11, query.count());

        query.orderBy("string_field desc").skip(2).limit(5);

        List<SequenceIdEntity> entities = query.fetch();
        assertEquals(5, entities.size());
        assertEquals("value27", entities.get(0).stringField);
    }
}
