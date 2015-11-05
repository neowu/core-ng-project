package core.framework.impl.db;

import core.framework.api.db.Repository;
import core.framework.api.util.Lists;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author neo
 */
public class RepositoryImplCompositeKeyEntityTest {
    private static DatabaseImpl database;
    private static Repository<CompositeKeyEntity> repository;

    @BeforeClass
    public static void createDatabase() {
        database = new DatabaseImpl();
        database.url("jdbc:hsqldb:mem:.;sql.syntax_mys=true");
        database.execute("CREATE TABLE composite_key_entity (id1 VARCHAR(36), id2 VARCHAR(36), boolean_field BIT(1), long_field BIGINT, PRIMARY KEY (id1, id2))");

        repository = database.repository(CompositeKeyEntity.class);
    }

    @AfterClass
    public static void cleanupDatabase() {
        database.execute("DROP TABLE composite_key_entity");
    }

    @Before
    public void truncateTable() {
        database.execute("TRUNCATE TABLE composite_key_entity");
    }

    @Test
    public void insert() {
        CompositeKeyEntity entity = new CompositeKeyEntity();
        entity.id1 = "id1";
        entity.id2 = "id2";
        entity.booleanField = true;
        entity.longField = 1L;

        repository.insert(entity);

        CompositeKeyEntity selectedEntity = repository.get(entity.id1, entity.id2).get();

        Assert.assertEquals(entity.id1, selectedEntity.id1);
        Assert.assertEquals(entity.id2, selectedEntity.id2);
        Assert.assertEquals(entity.booleanField, selectedEntity.booleanField);
        Assert.assertEquals(entity.longField, selectedEntity.longField);
    }

    @Test
    public void update() {
        CompositeKeyEntity entity = new CompositeKeyEntity();
        entity.id1 = "id1";
        entity.id2 = "id2";
        entity.booleanField = true;
        entity.longField = 1L;
        repository.insert(entity);

        entity.longField = 2L;
        repository.update(entity);

        CompositeKeyEntity selectedEntity = repository.get(entity.id1, entity.id2).get();
        Assert.assertEquals(entity.longField, selectedEntity.longField);
    }

    @Test
    public void batchDelete() {
        CompositeKeyEntity entity1 = new CompositeKeyEntity();
        entity1.id1 = "1-1";
        entity1.id2 = "1-2";
        entity1.booleanField = true;
        CompositeKeyEntity entity2 = new CompositeKeyEntity();
        entity2.id1 = "2-1";
        entity2.id2 = "2-2";
        entity2.booleanField = true;
        repository.batchInsert(Lists.newArrayList(entity1, entity2));

        repository.batchDelete(Lists.newArrayList(new Object[]{entity1.id1, entity1.id2}, new Object[]{entity2.id1, entity2.id2}));

        Assert.assertFalse(repository.get(entity1.id1, entity1.id2).isPresent());
        Assert.assertFalse(repository.get(entity2.id1, entity2.id2).isPresent());
    }
}