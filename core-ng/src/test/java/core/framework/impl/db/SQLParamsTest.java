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
        var mapper = new EnumDBMapper();
        mapper.registerEnumClass(TestEnum.class);
        var params = new SQLParams(mapper, "String", 1, TestEnum.V2, LocalDate.of(2018, 6, 1));

        assertThat(params.toString())
                .isEqualTo("[String, 1, DB_V2, 2018-06-01]");
    }

    @Test
    void convertToStringWithUnregisteredEnum() {
        assertThat(new SQLParams(new EnumDBMapper(), TestEnum.V1).toString())
                .isEqualTo("[V1]");
    }

    @Test
    void convertToStringWithEmpty() {
        assertThat(new SQLParams(null).toString()).isEqualTo("[]");
    }

    @Test
    void convertToStringWithNull() {
        assertThat(new SQLParams(null, (Object[]) null).toString())
                .isEqualTo("null");
    }
}
