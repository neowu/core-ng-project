package core.framework.impl.db;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class SQLParamsTest {
    @Test
    void toStringWithEnum() {
        EnumDBMapper mapper = new EnumDBMapper();
        mapper.registerEnumClass(TestEnum.class);
        SQLParams params = new SQLParams(mapper, "String", 1, TestEnum.V2, LocalDate.of(2018, 6, 1));
        assertThat(params.toString()).isEqualTo("[String, 1, DB_V2, 2018-06-01]");
    }

    @Test
    void toStringWithEmpty() {
        SQLParams params = new SQLParams(null);
        assertThat(params.toString()).isEqualTo("[]");
    }

    @Test
    void toStringWithNull() {
        SQLParams params = new SQLParams(null, (Object[]) null);
        assertThat(params.toString()).isEqualTo("null");
    }
}
