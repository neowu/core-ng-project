package core.framework.impl.db;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class SQLParamsTest {
    @Test
    void appendWithEnum() {
        var mapper = new EnumDBMapper();
        mapper.registerEnumClass(TestEnum.class);

        var params = new SQLParams(mapper, "String", 1, TestEnum.V2, LocalDate.of(2018, 6, 1));
        var builder = new StringBuilder();
        params.append(builder, Set.of(), 1000);
        assertThat(builder.toString())
                .isEqualTo("[String, 1, DB_V2, 2018-06-01]");
    }

    @Test
    void appendWithUnregisteredEnum() {
        var params = new SQLParams(new EnumDBMapper(), TestEnum.V1);
        var builder = new StringBuilder();
        params.append(builder, Set.of(), 1000);
        assertThat(builder.toString())
                .isEqualTo("[V1]");
    }

    @Test
    void appendWithEmpty() {
        var params = new SQLParams(null);
        var builder = new StringBuilder();
        params.append(builder, Set.of(), 1000);
        assertThat(builder.toString()).isEqualTo("[]");
    }

    @Test
    void appendWithNull() {
        var params = new SQLParams(null, (Object[]) null);
        var builder = new StringBuilder();
        params.append(builder, Set.of(), 1000);
        assertThat(builder.toString())
                .isEqualTo("null");
    }
}
