package core.framework.impl.db;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class SQLParamsTest {
    @Test
    void convertToStringWithEnum() {
        EnumDBMapper mapper = new EnumDBMapper();
        mapper.registerEnumClass(TestEnum.class);
        SQLParams params = new SQLParams(mapper, "String", 1, TestEnum.V2, LocalDate.of(2018, 6, 1));
        assertThat(params.toString()).isEqualTo("[String, 1, DB_V2, 2018-06-01]");
    }

    @Test
    void convertToStringWithUnregisteredEnum() {
        EnumDBMapper mapper = new EnumDBMapper();
        SQLParams params = new SQLParams(mapper, TestEnum.V1);
        assertThat(params.toString()).isEqualTo("[V1]");
    }

    @Test
    void convertToStringWithEmpty() {
        SQLParams params = new SQLParams(null);
        assertThat(params.toString()).isEqualTo("[]");
    }

    @Test
    void convertToStringWithNull() {
        SQLParams params = new SQLParams(null, (Object[]) null);
        assertThat(params.toString()).isEqualTo("null");
    }
}
