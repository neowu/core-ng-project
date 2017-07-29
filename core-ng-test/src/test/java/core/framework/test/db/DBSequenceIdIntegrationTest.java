package core.framework.test.db;

import core.framework.api.db.Repository;
import core.framework.test.IntegrationTest;
import org.junit.Test;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author neo
 */
public class DBSequenceIdIntegrationTest extends IntegrationTest {
    @Inject
    @Named("seq")
    Repository<TestSequenceIdDBEntity> entityRepository;

    @Test
    public void insert() {
        TestSequenceIdDBEntity entity = new TestSequenceIdDBEntity();
        entity.intField = 1;
        Optional<Long> id = entityRepository.insert(entity);

        assertTrue(id.isPresent());
        TestSequenceIdDBEntity selectedEntity = entityRepository.get(id.get()).get();
        assertEquals(entity.intField, selectedEntity.intField);
    }
}
