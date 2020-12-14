package core.framework.internal.web.bean;

import core.framework.web.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    void toStringWithBoolean() {
        assertThat(QueryParamHelper.toString((Boolean) null)).isNull();
        assertThat(QueryParamHelper.toString(Boolean.TRUE)).isEqualTo("true");
    }

    @Test
    void toDateTime() {
        assertThat(QueryParamHelper.toDate("")).isNull();
        assertThat(QueryParamHelper.toTime("")).isNull();
        assertThat(QueryParamHelper.toDateTime("")).isNull();
        assertThat(QueryParamHelper.toZonedDateTime("")).isNull();

        assertThat(QueryParamHelper.toDateTime("2020-12-14T11:16:00")).isEqualTo("2020-12-14T11:16:00");
        assertThat(QueryParamHelper.toZonedDateTime("2020-12-14T11:16:00Z")).isEqualTo("2020-12-14T11:16:00Z");

        assertThatThrownBy(() -> QueryParamHelper.toDate("invalid")).isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> QueryParamHelper.toTime("invalid")).isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> QueryParamHelper.toDateTime("invalid")).isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> QueryParamHelper.toZonedDateTime("invalid")).isInstanceOf(BadRequestException.class);
    }

    @Test
    void toDouble() {
        assertThatThrownBy(() -> QueryParamHelper.toDouble("invalid")).isInstanceOf(BadRequestException.class);
    }

    @Test
    void toBigDecimal() {
        assertThatThrownBy(() -> QueryParamHelper.toBigDecimal("invalid")).isInstanceOf(BadRequestException.class);
    }

    @Test
    void toBoolean() {
        assertThat(QueryParamHelper.toBoolean("true")).isTrue();
        assertThat(QueryParamHelper.toBoolean("false")).isFalse();
        assertThat(QueryParamHelper.toBoolean("invalid")).isFalse();
    }

    @Test
    void toEnum() {
        assertThat(QueryParamHelper.toEnum("", TestQueryParamBean.TestEnum.class)).isNull();
        assertThat(QueryParamHelper.toEnum("V1", TestQueryParamBean.TestEnum.class)).isEqualTo(TestQueryParamBean.TestEnum.VALUE1);
        assertThatThrownBy(() -> QueryParamHelper.toEnum("INVALID", TestQueryParamBean.TestEnum.class)).isInstanceOf(BadRequestException.class);
    }
}
