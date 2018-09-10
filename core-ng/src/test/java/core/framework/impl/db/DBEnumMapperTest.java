package core.framework.impl.db;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
    void testGetEnum() {
        assertThat(mapper.getEnum(null)).isNull();
        assertThat(mapper.getEnum("DB_V1")).isEqualTo(TestEnum.V1);
        assertThat(mapper.getEnum("DB_V2")).isEqualTo(TestEnum.V2);
    }
}
