package core.framework.impl.json;

import org.junit.Test;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnumValue;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author neo
 */
public class JSONMapperTest {
    @Test
    public void enumValue() {
        assertEquals(TestEnum.A, JSONMapper.fromJSONValue(TestEnum.class, "A1"));
        assertEquals("B1", JSONMapper.toJSONValue(TestEnum.B));
    }

    @Test
    public void map() {
        TestMapBean bean = new TestMapBean();
        bean.stringField = "value";
        bean.dateTimeField = LocalDateTime.of(2016, Month.FEBRUARY, 15, 0, 0, 0);
        bean.numberField = 1;
        Map<String, String> map = JSONMapper.toMapValue(bean);
        assertEquals("value", map.get("string_field"));
        assertEquals("2016-02-15T00:00:00", map.get("date_time_field"));
        assertEquals("1", map.get("number_field"));
        assertNull(map.get("null_field"));

        TestMapBean convertedBean = JSONMapper.fromMapValue(TestMapBean.class, map);
        assertEquals(bean.stringField, convertedBean.stringField);
        assertEquals(bean.dateTimeField, convertedBean.dateTimeField);
        assertEquals(bean.numberField, convertedBean.numberField);
        assertNull(convertedBean.nullField);
    }

    enum TestEnum {
        @XmlEnumValue("A1")
        A,
        @XmlEnumValue("B1")
        B
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    static class TestMapBean {
        @XmlElement(name = "string_field")
        public String stringField;

        @XmlElement(name = "date_time_field")
        public LocalDateTime dateTimeField;

        @XmlElement(name = "number_field")
        public Integer numberField;

        @XmlElement(name = "null_field")
        public String nullField;
    }
}