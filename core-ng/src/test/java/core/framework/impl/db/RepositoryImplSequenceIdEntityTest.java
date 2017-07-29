package core.framework.impl.db;

import core.framework.api.db.Repository;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Optional;

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

        Assert.assertEquals((long) id.get(), (long) selectedEntity.id);
        Assert.assertEquals(entity.stringField, selectedEntity.stringField);
    }
}
