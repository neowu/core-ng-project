package core.framework.impl.json;

import org.junit.Test;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author neo
 */
public class JSONMapperTest {
    @Test
    public void map() {
        TestBean bean = new TestBean();
        bean.stringField = "value";
        bean.dateTimeField = LocalDateTime.of(2016, Month.FEBRUARY, 15, 0, 0, 0);
        bean.zonedDateTimeField = ZonedDateTime.of(LocalDate.of(2017, Month.AUGUST, 7), LocalTime.of(16, 20, 30), ZoneId.of("UTC"));
        bean.numberField = 1;
        Map<String, String> map = JSONMapper.toMapValue(bean);
        assertEquals("value", map.get("string_field"));
        assertEquals("2016-02-15T00:00:00", map.get("date_time_field"));
        assertEquals("2017-08-07T16:20:30Z", map.get("zoned_date_time_field"));
        assertEquals("1", map.get("number_field"));
        assertNull(map.get("null_field"));

        TestBean convertedBean = JSONMapper.fromMapValue(TestBean.class, map);
        assertEquals(bean.stringField, convertedBean.stringField);
        assertEquals(bean.dateTimeField, convertedBean.dateTimeField);
        assertEquals(bean.numberField, convertedBean.numberField);
        assertNull(convertedBean.nullField);
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    static class TestBean {
        @XmlElement(name = "string_field")
        public String stringField;

        @XmlElement(name = "date_time_field")
        public LocalDateTime dateTimeField;

        @XmlElement(name = "zoned_date_time_field")
        public ZonedDateTime zonedDateTimeField;

        @XmlElement(name = "number_field")
        public Integer numberField;

        @XmlElement(name = "null_field")
        public String nullField;
    }
}
