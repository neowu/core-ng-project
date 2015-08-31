package core.framework.impl.db;

import core.framework.api.db.Repository;
import core.framework.api.util.Lists;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * @author neo
 */
public class RepositoryImplTest {
    static DatabaseImpl database;
    static Repository<EntityWithAssignedId> entityWithAssignedIdRepository;
    static Repository<EntityWithAutoIncrementId> entityWithAutoIncrementIdRepository;

    @BeforeClass
    public static void createDatabase() {
        database = new DatabaseImpl();
        database.url("jdbc:hsqldb:mem:.;sql.syntax_mys=true");
        database.execute("CREATE TABLE entity_with_auto_increment_id (id INT AUTO_INCREMENT PRIMARY KEY, string_field VARCHAR(20), double_field DOUBLE, enum_field VARCHAR(10), date_time_field TIMESTAMP)");
        database.execute("CREATE TABLE entity_with_assigned_id (id VARCHAR(36) PRIMARY KEY, string_field VARCHAR(20), int_field INT, big_decimal_field DECIMAL(10,2))");

        entityWithAssignedIdRepository = database.repository(EntityWithAssignedId.class);
        entityWithAutoIncrementIdRepository = database.repository(EntityWithAutoIncrementId.class);
    }

    @AfterClass
    public static void cleanupDatabase() {
        database.execute("DROP TABLE entity_with_auto_increment_id");
        database.execute("DROP TABLE entity_with_assigned_id");
    }

    @Test
    public void insertWithAutoIncrementIdEntity() {
        EntityWithAutoIncrementId entity = new EntityWithAutoIncrementId();
        entity.stringField = "string";
        entity.doubleField = 3.25;
        entity.dateTimeField = LocalDateTime.now();

        Optional<Long> id = entityWithAutoIncrementIdRepository.insert(entity);
        Assert.assertTrue(id.isPresent());

        EntityWithAutoIncrementId selectedEntity = entityWithAutoIncrementIdRepository.get(id.get()).get();

        Assert.assertEquals((long) id.get(), (long) selectedEntity.id);
        Assert.assertEquals(entity.stringField, selectedEntity.stringField);
        Assert.assertEquals(entity.doubleField, selectedEntity.doubleField);
        Assert.assertEquals(entity.dateTimeField, selectedEntity.dateTimeField);
    }

    @Test
    public void insertWithAssignedIdEntity() {
        EntityWithAssignedId entity = new EntityWithAssignedId();
        entity.id = UUID.randomUUID().toString();
        entity.stringField = "string";
        entity.intField = 12;
        entity.bigDecimalField = new BigDecimal("86.99");

        Optional<Long> id = entityWithAssignedIdRepository.insert(entity);
        Assert.assertFalse(id.isPresent());

        EntityWithAssignedId selectedEntity = entityWithAssignedIdRepository.get(entity.id).get();

        Assert.assertEquals(entity.id, selectedEntity.id);
        Assert.assertEquals(entity.stringField, selectedEntity.stringField);
        Assert.assertEquals(entity.intField, selectedEntity.intField);
        Assert.assertEquals(entity.bigDecimalField, selectedEntity.bigDecimalField);
    }

    @Test
    public void selectOne() {
        EntityWithAutoIncrementId entity = new EntityWithAutoIncrementId();
        entity.stringField = "stringField#123456";

        Optional<Long> id = entityWithAutoIncrementIdRepository.insert(entity);
        Assert.assertTrue(id.isPresent());

        EntityWithAutoIncrementId selectedEntity = entityWithAutoIncrementIdRepository.selectOne("string_field = ?", entity.stringField).get();

        Assert.assertEquals((long) id.get(), (long) selectedEntity.id);
        Assert.assertEquals(entity.stringField, selectedEntity.stringField);
    }

    @Test
    public void update() {
        EntityWithAssignedId entity = new EntityWithAssignedId();
        entity.id = UUID.randomUUID().toString();
        entity.stringField = "string";
        entity.intField = 11;
        entityWithAssignedIdRepository.insert(entity);

        EntityWithAssignedId updatedEntity = new EntityWithAssignedId();
        updatedEntity.id = entity.id;
        updatedEntity.stringField = "updated";
        entityWithAssignedIdRepository.update(updatedEntity);

        EntityWithAssignedId selectedEntity = entityWithAssignedIdRepository.get(entity.id).get();
        Assert.assertEquals(updatedEntity.stringField, selectedEntity.stringField);
        Assert.assertEquals(entity.intField, selectedEntity.intField);
    }

    @Test
    public void delete() {
        EntityWithAssignedId entity = new EntityWithAssignedId();
        entity.id = UUID.randomUUID().toString();
        entity.intField = 11;
        entityWithAssignedIdRepository.insert(entity);

        entityWithAssignedIdRepository.delete(entity.id);

        Optional<EntityWithAssignedId> result = entityWithAssignedIdRepository.get(entity.id);
        Assert.assertFalse(result.isPresent());
    }

    @Test
    public void batchInsert() {
        EntityWithAssignedId entity1 = new EntityWithAssignedId();
        entity1.id = "1";
        entity1.stringField = "value1";
        entity1.intField = 11;

        EntityWithAssignedId entity2 = new EntityWithAssignedId();
        entity2.id = "2";
        entity2.stringField = "value2";
        entity2.intField = 12;

        entityWithAssignedIdRepository.batchInsert(Lists.newArrayList(entity1, entity2));

        EntityWithAssignedId selectedEntity1 = entityWithAssignedIdRepository.get("1").get();
        Assert.assertEquals(entity1.stringField, selectedEntity1.stringField);

        EntityWithAssignedId selectedEntity2 = entityWithAssignedIdRepository.get("2").get();
        Assert.assertEquals(entity2.stringField, selectedEntity2.stringField);
    }

    @Test
    public void batchDelete() {
        EntityWithAssignedId entity1 = new EntityWithAssignedId();
        entity1.id = "3";
        entity1.intField = 11;
        EntityWithAssignedId entity2 = new EntityWithAssignedId();
        entity2.id = "4";
        entity2.intField = 12;
        entityWithAssignedIdRepository.batchInsert(Lists.newArrayList(entity1, entity2));

        entityWithAssignedIdRepository.batchDelete(Lists.newArrayList("3", "4"));

        Assert.assertFalse(entityWithAssignedIdRepository.get("3").isPresent());
        Assert.assertFalse(entityWithAssignedIdRepository.get("4").isPresent());
    }
}