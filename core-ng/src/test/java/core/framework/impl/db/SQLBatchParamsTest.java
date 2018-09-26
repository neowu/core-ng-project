package core.framework.impl.db;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class SQLBatchParamsTest {
    @Test
    void append() {
        var mapper = new EnumDBMapper();
        mapper.registerEnumClass(TestEnum.class);

        var params = new SQLBatchParams(mapper, List.of(new Object[]{"param1", 1, TestEnum.V1},
                new Object[]{"param2", 2, TestEnum.V2},
                new Object[]{"param3", 3, null}));
        var builder = new StringBuilder();
        params.append(builder, Set.of(), 10000);

        assertThat(builder.toString())
                .isEqualTo("[[param1, 1, DB_V1], [param2, 2, DB_V2], [param3, 3, null]]");
    }

    @Test
    void appendWithTruncation() {
        var params = new SQLBatchParams(new EnumDBMapper(), List.of(new Object[]{"param1", 1},
                new Object[]{"param2", 2},
                new Object[]{"param3", 3}));
        var builder = new StringBuilder();
        params.append(builder, Set.of(), 10);
        assertThat(builder.toString())
                .hasSize(10 + "...(truncated)".length())
                .isEqualTo("[[param1, ...(truncated)");
    }
}
