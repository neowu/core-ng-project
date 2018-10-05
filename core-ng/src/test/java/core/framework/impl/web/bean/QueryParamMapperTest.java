package core.framework.impl.web.bean;

import core.framework.util.ClasspathResources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class QueryParamMapperTest {
    private QueryParamMapperBuilder<TestQueryParamBean> builder;
    private QueryParamMapper<TestQueryParamBean> mapper;

    @BeforeEach
    void createMapper() {
        builder = new QueryParamMapperBuilder<>(TestQueryParamBean.class);
        mapper = builder.build();
    }

    @Test
    void sourceCode() {
        String sourceCode = builder.builder.sourceCode();
        assertEquals(ClasspathResources.text("query-param-mapper-test/test-bean-mapper.java"), sourceCode);
    }

    @Test
    void convertBeanToParams() {
        var bean = new TestQueryParamBean();
        bean.stringField = "value";
        bean.intField = 12;
        bean.doubleField = 22.3;
        bean.dateTimeField = LocalDateTime.of(2017, 8, 28, 13, 44, 0);
        bean.enumField = TestQueryParamBean.TestEnum.VALUE2;

        Map<String, String> params = mapper.toParams(bean);

        assertThat(params).containsEntry("int_field", "12")
                          .containsEntry("double_field", "22.3")
                          .containsEntry("string_field", "value")
                          .containsEntry("date_time_field", "2017-08-28T13:44:00")
                          .containsEntry("enum_field", "V2")
                          .containsEntry("default_value_field", "value");
    }

    @Test
    void fromParams() {
        var params = Map.of("boolean_field", "true",
                "big_decimal_field", "345.67",
                "date_field", "2017-08-28",
                "long_field", "123",
                "double_field", "");

        TestQueryParamBean bean = mapper.fromParams(params);

        assertThat(bean.booleanField).isTrue();
        assertThat(bean.bigDecimalField).isEqualTo("345.67");
        assertThat(bean.dateField).isEqualTo(LocalDate.of(2017, 8, 28));
        assertThat(bean.longField).isEqualTo(123);
        assertThat(bean.doubleField).isNull();
        assertThat(bean.defaultValueField).isEqualTo("value");
    }
}
