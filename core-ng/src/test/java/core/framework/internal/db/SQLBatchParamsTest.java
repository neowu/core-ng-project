package core.framework.internal.db;

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
        var params = new SQLBatchParams(new EnumDBMapper(), List.of(new Object[]{"v01-long-text", 1},
                new Object[]{"v02-long-text", 2},
                new Object[]{"v03-long-text", 3},
                new Object[]{"v04-long-text", 4}));
        var builder = new StringBuilder();
        params.append(builder, Set.of(), 40);
        assertThat(builder.toString())
                .hasSize(40 + "...(truncated)".length())
                .isEqualTo("[[v01-...(truncated), 1], [v02-...(trunc...(truncated)");
    }
}
