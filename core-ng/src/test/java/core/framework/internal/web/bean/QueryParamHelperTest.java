package core.framework.internal.web.bean;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class QueryParamHelperTest {
    @Test
    void toStringWithString() {
        assertThat(QueryParamHelper.toString("")).as("empty string will be treated as null").isNull();
        assertThat(QueryParamHelper.toString(" ")).isEqualTo(" ");
        assertThat(QueryParamHelper.toString("value")).isEqualTo("value");
    }

    @Test
    void toStringWithDate() {
        assertThat(QueryParamHelper.toString(LocalDate.of(2020, Month.JULY, 31))).isEqualTo("2020-07-31");
        assertThat(QueryParamHelper.toString(ZonedDateTime.of(LocalDateTime.of(2020, Month.JULY, 31, 12, 0, 0), ZoneId.of("UTC")))).isEqualTo("2020-07-31T12:00:00Z");
    }
}
