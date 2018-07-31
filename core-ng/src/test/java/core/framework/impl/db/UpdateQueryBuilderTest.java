package core.framework.impl.db;

import core.framework.util.ClasspathResources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class UpdateQueryBuilderTest {
    private UpdateQueryBuilder<AutoIncrementIdEntity> builder;
    private UpdateQuery<AutoIncrementIdEntity> updateQuery;

    @BeforeEach
    void createUpdateQuery() {
        builder = new UpdateQueryBuilder<>(AutoIncrementIdEntity.class);
        updateQuery = builder.build();
    }

    @Test
    void sourceCode() {
        String sourceCode = builder.builder.sourceCode();
        assertEquals(ClasspathResources.text("db-test/update-query-auto-increment-id.java"), sourceCode);
    }

    @Test
    void update() {
        AutoIncrementIdEntity entity = new AutoIncrementIdEntity();
        entity.id = 1;
        entity.stringField = "new_value";
        UpdateQuery.Statement statement = updateQuery.update(entity, true);

        assertEquals("UPDATE auto_increment_id_entity SET string_field = ? WHERE id = ?", statement.sql);
        assertEquals(2, statement.params.length);
        assertEquals(entity.stringField, statement.params[0]);
        assertEquals(entity.id, statement.params[1]);
    }
}
