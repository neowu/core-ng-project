package core.framework.api.util;

import org.junit.Test;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author neo
 */
public class JSONTest {
    @Test
    public void fromJSONArray() {
        List<Bean> beans = JSON.fromJSON(Types.list(Bean.class), "[{\"name\":\"n1\"},{\"name\":\"n2\"}]");

        assertEquals(2, beans.size());
        assertEquals("n1", beans.get(0).name);
        assertEquals("n2", beans.get(1).name);
    }

    @Test
    public void mapField() {
        BeanWithMapField bean = new BeanWithMapField();
        bean.attributes.put("key1", "value1");
        bean.attributes.put("key2", "value2");
        String json = JSON.toJSON(bean);
        assertEquals("{\"attributes\":{\"key1\":\"value1\",\"key2\":\"value2\"}}", json);

        BeanWithMapField parsedBean = JSON.fromJSON(BeanWithMapField.class, json);
        assertEquals("value1", parsedBean.attributes.get("key1"));
        assertEquals("value2", parsedBean.attributes.get("key2"));
    }

    @Test
    public void dateField() {
        BeanWithDateField bean = new BeanWithDateField();
        bean.instant = Instant.now();
        bean.dateTime = LocalDateTime.ofInstant(bean.instant, ZoneId.systemDefault());
        bean.date = bean.dateTime.toLocalDate();
        String json = JSON.toJSON(bean);

        BeanWithDateField parsedBean = JSON.fromJSON(BeanWithDateField.class, json);
        assertEquals(bean.instant, parsedBean.instant);
        assertEquals(bean.date, parsedBean.date);
        assertEquals(bean.dateTime, parsedBean.dateTime);
    }

    @Test
    public void nullObject() {
        String json = JSON.toJSON(null);
        Bean bean = JSON.fromJSON(Bean.class, json);

        assertNull(bean);
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    static class Bean {
        @XmlElement(name = "name")
        public String name;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    static class BeanWithMapField {
        @XmlElement(name = "attributes")
        public final Map<String, String> attributes = Maps.newHashMap();
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    static class BeanWithDateField {
        @XmlElement(name = "date")
        public LocalDate date;

        @XmlElement(name = "dateTime")
        public LocalDateTime dateTime;

        @XmlElement(name = "instant")
        public Instant instant;
    }
}