package core.framework.impl.db;

import core.framework.util.ClasspathResources;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author neo
 */
public class UpdateQueryBuilderTest {
    private UpdateQueryBuilder<AutoIncrementIdEntity> builder;
    private UpdateQuery<AutoIncrementIdEntity> updateQuery;

    @Before
    public void createUpdateQuery() {
        builder = new UpdateQueryBuilder<>(AutoIncrementIdEntity.class);
        updateQuery = builder.build();
    }

    @Test
    public void sourceCode() {
        String sourceCode = builder.builder.sourceCode();
        assertEquals(ClasspathResources.text("db-test/update-query-auto-increment-id.java"), sourceCode);
    }

    @Test
    public void update() {
        AutoIncrementIdEntity entity = new AutoIncrementIdEntity();
        entity.id = 1;
        entity.stringField = "new_value";
        UpdateQuery.Statement statement = updateQuery.update(entity);

        assertEquals("UPDATE auto_increment_id_entity SET string_field = ? WHERE id = ?", statement.sql);
        assertEquals(2, statement.params.length);
        assertEquals(entity.stringField, statement.params[0]);
        assertEquals(entity.id, statement.params[1]);
    }
}
