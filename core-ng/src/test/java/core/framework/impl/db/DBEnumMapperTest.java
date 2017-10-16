package core.framework.impl.db;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author neo
 */
class DBEnumMapperTest {
    private DBEnumMapper<TestEnum> mapper;

    @BeforeEach
    void createDBEnumMapper() {
        mapper = new DBEnumMapper<>(TestEnum.class);
    }

    @Test
    void getEnum() {
        assertNull(mapper.getEnum(null));
        assertEquals(TestEnum.V1, mapper.getEnum("DB_V1"));
        assertEquals(TestEnum.V2, mapper.getEnum("DB_V2"));
    }
}
