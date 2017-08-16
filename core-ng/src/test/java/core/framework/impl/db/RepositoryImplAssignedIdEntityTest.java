package core.framework.impl.db;

import core.framework.api.db.Repository;
import core.framework.api.util.Lists;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Optional;
import java.util.UUID;

/**
 * @author neo
 */
public class RepositoryImplAssignedIdEntityTest {
    private static DatabaseImpl database;
    private static Repository<AssignedIdEntity> repository;

    @BeforeClass
    public static void createDatabase() {
        database = new DatabaseImpl();
        database.url("jdbc:hsqldb:mem:.;sql.syntax_mys=true");
        database.vendor = Vendor.MYSQL;
        database.execute("CREATE TABLE assigned_id_entity (id VARCHAR(36) PRIMARY KEY, string_field VARCHAR(20), int_field INT, big_decimal_field DECIMAL(10,2), date_field DATE)");

        repository = database.repository(AssignedIdEntity.class);
    }

    @AfterClass
    public static void cleanupDatabase() {
        database.execute("DROP TABLE assigned_id_entity");
    }

    @Before
    public void truncateTable() {
        database.execute("TRUNCATE TABLE assigned_id_entity");
    }

    @Test
    public void insert() {
        AssignedIdEntity entity = new AssignedIdEntity();
        entity.id = UUID.randomUUID().toString();
        entity.stringField = "string";
        entity.intField = 12;
        entity.bigDecimalField = new BigDecimal("86.99");
        entity.dateField = LocalDate.of(2016, Month.JULY, 5);

        Optional<Long> id = repository.insert(entity);
        Assert.assertFalse(id.isPresent());

        AssignedIdEntity selectedEntity = repository.get(entity.id).get();

        Assert.assertEquals(entity.id, selectedEntity.id);
        Assert.assertEquals(entity.stringField, selectedEntity.stringField);
        Assert.assertEquals(entity.intField, selectedEntity.intField);
        Assert.assertEquals(entity.bigDecimalField, selectedEntity.bigDecimalField);
        Assert.assertEquals(entity.dateField, selectedEntity.dateField);
    }

    @Test
    public void update() {
        AssignedIdEntity entity = new AssignedIdEntity();
        entity.id = UUID.randomUUID().toString();
        entity.stringField = "string";
        entity.intField = 11;
        repository.insert(entity);

        AssignedIdEntity updatedEntity = new AssignedIdEntity();
        updatedEntity.id = entity.id;
        updatedEntity.stringField = "updated";
        updatedEntity.dateField = LocalDate.of(2016, Month.JULY, 5);
        repository.update(updatedEntity);

        AssignedIdEntity selectedEntity = repository.get(entity.id).get();
        Assert.assertEquals(updatedEntity.stringField, selectedEntity.stringField);
        Assert.assertEquals(entity.intField, selectedEntity.intField);
        Assert.assertEquals(updatedEntity.dateField, selectedEntity.dateField);
    }

    @Test
    public void delete() {
        AssignedIdEntity entity = new AssignedIdEntity();
        entity.id = UUID.randomUUID().toString();
        entity.intField = 11;
        repository.insert(entity);

        repository.delete(entity.id);

        Optional<AssignedIdEntity> result = repository.get(entity.id);
        Assert.assertFalse(result.isPresent());
    }

    @Test
    public void batchInsert() {
        AssignedIdEntity entity1 = new AssignedIdEntity();
        entity1.id = "1";
        entity1.stringField = "value1";
        entity1.intField = 11;

        AssignedIdEntity entity2 = new AssignedIdEntity();
        entity2.id = "2";
        entity2.stringField = "value2";
        entity2.intField = 12;

        repository.batchInsert(Lists.newArrayList(entity1, entity2));

        AssignedIdEntity selectedEntity1 = repository.get("1").get();
        Assert.assertEquals(entity1.stringField, selectedEntity1.stringField);

        AssignedIdEntity selectedEntity2 = repository.get("2").get();
        Assert.assertEquals(entity2.stringField, selectedEntity2.stringField);
    }

    @Test
    public void batchDelete() {
        AssignedIdEntity entity1 = new AssignedIdEntity();
        entity1.id = "3";
        entity1.intField = 11;
        AssignedIdEntity entity2 = new AssignedIdEntity();
        entity2.id = "4";
        entity2.intField = 12;
        repository.batchInsert(Lists.newArrayList(entity1, entity2));

        repository.batchDelete(Lists.newArrayList(entity1.id, entity2.id));

        Assert.assertFalse(repository.get(entity1.id).isPresent());
        Assert.assertFalse(repository.get(entity2.id).isPresent());
    }
}
