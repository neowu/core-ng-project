package core.framework.impl.web.bean;

import core.framework.util.Maps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author neo
 */
class QueryParamMapperTest {
    private QueryParamMapper<TestQueryParamBean> mapper;

    @BeforeEach
    void createMapper() {
        mapper = new QueryParamMapperBuilder<>(TestQueryParamBean.class).build();
    }

    @Test
    void toParams() {
        TestQueryParamBean bean = new TestQueryParamBean();
        bean.stringField = "value";
        bean.intField = 12;
        bean.doubleField = 22.3;
        bean.dateTimeField = LocalDateTime.of(2017, 8, 28, 13, 44, 0);
        bean.enumField = TestQueryParamBean.TestEnum.VALUE2;

        Map<String, String> params = mapper.toParams(bean);

        assertEquals("12", params.get("int_field"));
        assertEquals("22.3", params.get("double_field"));
        assertEquals("value", params.get("string_field"));
        assertEquals("2017-08-28T13:44:00", params.get("date_time_field"));
        assertEquals("V2", params.get("enum_field"));
    }

    @Test
    void fromParams() {
        Map<String, String> params = Maps.newHashMap();
        params.put("boolean_field", "true");
        params.put("big_decimal_field", "345.67");
        params.put("date_field", "2017-08-28");
        params.put("long_field", "123");

        TestQueryParamBean bean = mapper.fromParams(params);

        assertTrue(bean.booleanField);
        assertEquals(BigDecimal.valueOf(345.67), bean.bigDecimalField);
        assertEquals(LocalDate.of(2017, 8, 28), bean.dateField);
        assertEquals(Long.valueOf(123), bean.longField);
    }
}
