package core.framework.impl.db;

import core.framework.util.ClasspathResources;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author neo
 */
public class RowMapperBuilderTest {
    @Test
    public void sourceCode() {
        RowMapperBuilder<AutoIncrementIdEntity> builder = new RowMapperBuilder<>(AutoIncrementIdEntity.class, new EnumDBMapper());
        RowMapper<AutoIncrementIdEntity> mapper = builder.build();
        assertNotNull(mapper);
        String sourceCode = builder.builder.sourceCode();
        assertEquals(ClasspathResources.text("db-test/row-mapper-auto-increment-id.java"), sourceCode);
    }
}
