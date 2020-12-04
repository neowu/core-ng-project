package core.framework.internal.web.bean;

import core.framework.util.ClasspathResources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class QueryParamWriterTest {
    private QueryParamWriterBuilder<TestQueryParamBean> builder;
    private QueryParamWriter<TestQueryParamBean> writer;

    @BeforeEach
    void createMapper() {
        builder = new QueryParamWriterBuilder<>(TestQueryParamBean.class);
        writer = builder.build();
    }

    @Test
    void sourceCode() {
        String sourceCode = builder.builder.sourceCode();
        assertEquals(ClasspathResources.text("query-param-mapper-test/test-bean-writer.java"), sourceCode);
    }

    @Test
    void convertBeanToParams() {
        var bean = new TestQueryParamBean();
        bean.stringField = "value";
        bean.intField = 12;
        bean.doubleField = 22.3;
        bean.zonedDateTimeField = ZonedDateTime.parse("2020-12-04T09:00:00Z");
        bean.dateTimeField = LocalDateTime.of(2017, 8, 28, 13, 44, 0);
        bean.timeField = LocalTime.of(13, 1, 2);
        bean.enumField = TestQueryParamBean.TestEnum.VALUE2;

        Map<String, String> params = writer.toParams(bean);

        assertThat(params).containsEntry("int_field", "12")
                .containsEntry("double_field", "22.3")
                .containsEntry("string_field", "value")
                .containsEntry("zoned_date_time_field", "2020-12-04T09:00:00Z")
                .containsEntry("date_time_field", "2017-08-28T13:44:00")
                .containsEntry("time_field", "13:01:02")
                .containsEntry("enum_field", "V2")
                .containsEntry("default_value_field", "value");
    }
}
