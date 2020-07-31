package core.framework.internal.web.bean;

import core.framework.util.ClasspathResources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class QueryParamReaderTest {
    private QueryParamReaderBuilder<TestQueryParamBean> builder;
    private QueryParamReader<TestQueryParamBean> reader;

    @BeforeEach
    void createQueryParamReader() {
        builder = new QueryParamReaderBuilder<>(TestQueryParamBean.class);
        reader = builder.build();
    }

    @Test
    void sourceCode() {
        String sourceCode = builder.builder.sourceCode();
        assertThat(sourceCode).isEqualToIgnoringWhitespace(ClasspathResources.text("query-param-mapper-test/test-bean-reader.java"));
    }

    @Test
    void fromParams() {
        var params = Map.of("boolean_field", "true",
                "big_decimal_field", "345.67",
                "date_field", "2017-08-28",
                "time_field", "13:01:02",
                "long_field", "123",
                "double_field", "",
                "enum_field", "V1");

        TestQueryParamBean bean = reader.fromParams(params);

        assertThat(bean.booleanField).isTrue();
        assertThat(bean.bigDecimalField).isEqualTo("345.67");
        assertThat(bean.dateField).isEqualTo(LocalDate.of(2017, 8, 28));
        assertThat(bean.timeField).isEqualTo(LocalTime.of(13, 1, 2));
        assertThat(bean.longField).isEqualTo(123);
        assertThat(bean.doubleField).isNull();
        assertThat(bean.enumField).isEqualTo(TestQueryParamBean.TestEnum.VALUE1);
        assertThat(bean.defaultValueField).isEqualTo("value");
    }
}
