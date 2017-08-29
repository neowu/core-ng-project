package core.framework.impl.web.bean;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnumValue;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class TestBean {
    @XmlElement(name = "zoned_date_time_field")
    public ZonedDateTime zonedDateTimeField;

    @XmlElement(name = "date_time_field")
    public LocalDateTime dateTimeField;

    @XmlElement(name = "date_field")
    public LocalDate dateField;

    @XmlElement(name = "string_field")
    public String stringField;

    @XmlElement(name = "int_field")
    public Integer intField;

    @XmlElement(name = "big_decimal_field")
    public BigDecimal bigDecimalField;

    @XmlElement(name = "list_field")
    public List<String> listField;

    @XmlElement(name = "map_field")
    public Map<String, String> mapField;

    @XmlElement(name = "child_field")
    public Child childField;

    @XmlElement(name = "children_field")
    public List<Child> childrenField;

    @XmlElement(name = "enum_field")
    public TestEnum enumField;

    public enum TestEnum {
        @XmlEnumValue("V1")
        VALUE1,
        @XmlEnumValue("V2")
        VALUE2
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Child {
        @XmlElement(name = "boolean_field")
        public Boolean booleanField;
    }
}
