package core.framework.test.db;

import core.framework.api.db.Repository;
import core.framework.test.IntegrationTest;
import org.junit.Test;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * @author neo
 */
public class DBIntegrationTest extends IntegrationTest {
    @Inject
    Repository<TestDBEntity> entityRepository;

    @Test
    public void insert() {
        TestDBEntity entity = new TestDBEntity();
        entity.id = UUID.randomUUID().toString();
        entity.dateTimeField = LocalDateTime.now();
        entity.dateField = LocalDate.now();
        entityRepository.insert(entity);

        TestDBEntity selectedEntity = entityRepository.get(entity.id).get();
        assertEquals(entity.dateField, selectedEntity.dateField);
        assertEquals(entity.dateTimeField, selectedEntity.dateTimeField);
    }
}
