package core.framework.impl.db;

import core.framework.util.ClasspathResources;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author neo
 */
class RowMapperBuilderTest {
    @Test
    void sourceCode() {
        RowMapperBuilder<AutoIncrementIdEntity> builder = new RowMapperBuilder<>(AutoIncrementIdEntity.class, new EnumDBMapper());
        RowMapper<AutoIncrementIdEntity> mapper = builder.build();
        assertNotNull(mapper);
        String sourceCode = builder.builder.sourceCode();
        assertEquals(ClasspathResources.text("db-test/row-mapper-auto-increment-id.java"), sourceCode);
    }
}
