package core.framework.impl.web.bean;

import core.framework.api.json.Property;
import core.framework.api.validate.NotNull;
import core.framework.api.web.service.QueryParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

/**
 * @author neo
 */
public class TestQueryParamBean {
    @QueryParam(name = "zoned_date_time_field")
    public ZonedDateTime zonedDateTimeField;

    @QueryParam(name = "date_time_field")
    public LocalDateTime dateTimeField;

    @QueryParam(name = "date_field")
    public LocalDate dateField;

    @QueryParam(name = "string_field")
    public String stringField;

    @QueryParam(name = "int_field")
    public Integer intField;

    @QueryParam(name = "long_field")
    public Long longField;

    @QueryParam(name = "double_field")
    public Double doubleField;

    @QueryParam(name = "big_decimal_field")
    public BigDecimal bigDecimalField;

    @QueryParam(name = "boolean_field")
    public Boolean booleanField;

    @QueryParam(name = "enum_field")
    public TestEnum enumField;

    @NotNull
    @QueryParam(name = "default_value_field")
    public String defaultValueField = "value";

    public enum TestEnum {
        @Property(name = "V1")
        VALUE1,
        @Property(name = "V2")
        VALUE2
    }
}
