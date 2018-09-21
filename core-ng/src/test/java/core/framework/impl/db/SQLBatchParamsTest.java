package core.framework.impl.db;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class SQLBatchParamsTest {
    @Test
    void convertToString() {
        var mapper = new EnumDBMapper();
        mapper.registerEnumClass(TestEnum.class);
        List<Object[]> params = List.of(new Object[]{"param1", 1, TestEnum.V1},
                new Object[]{"param2", 2, TestEnum.V2},
                new Object[]{"param3", 3, null});

        assertThat(new SQLBatchParams(mapper, params).toString())
                .isEqualTo("[[param1, 1, DB_V1], [param2, 2, DB_V2], [param3, 3, null]]");
    }
}
