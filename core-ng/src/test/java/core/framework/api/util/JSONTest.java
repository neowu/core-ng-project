package core.framework.api.util;

import org.junit.Test;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnumValue;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author neo
 */
public class JSONTest {
    @Test
    public void mapField() {
        Bean bean = new Bean();
        bean.attributes.put("key1", "value1");
        bean.attributes.put("key2", "value2");
        String json = JSON.toJSON(bean);
        assertThat(json, containsString("\"attributes\":{\"key1\":\"value1\",\"key2\":\"value2\"}"));

        Bean parsedBean = JSON.fromJSON(Bean.class, json);
        assertEquals("value1", parsedBean.attributes.get("key1"));
        assertEquals("value2", parsedBean.attributes.get("key2"));
    }

    @Test
    public void dateField() {
        Bean bean = new Bean();
        bean.instant = Instant.now();
        bean.dateTime = LocalDateTime.ofInstant(bean.instant, ZoneId.systemDefault());
        bean.date = bean.dateTime.toLocalDate();
        bean.zonedDateTime = ZonedDateTime.ofInstant(bean.instant, ZoneId.systemDefault());
        String json = JSON.toJSON(bean);

        Bean parsedBean = JSON.fromJSON(Bean.class, json);
        assertEquals(bean.instant, parsedBean.instant);
        assertEquals(bean.date, parsedBean.date);
        assertEquals(bean.dateTime, parsedBean.dateTime);
        assertEquals(bean.zonedDateTime.toInstant(), parsedBean.zonedDateTime.toInstant());
    }

    @Test
    public void listObject() {
        List<Bean> beans = JSON.fromJSON(Types.list(Bean.class), "[{\"name\":\"n1\"},{\"name\":\"n2\"}]");

        assertEquals(2, beans.size());
        assertEquals("n1", beans.get(0).name);
        assertEquals("n2", beans.get(1).name);
    }

    @Test
    public void optionalObject() {
        Optional<Bean> parsedBean = JSON.fromJSON(Types.optional(Bean.class), JSON.toJSON(Optional.empty()));
        assertFalse(parsedBean.isPresent());

        parsedBean = JSON.fromJSON(Types.optional(Bean.class), JSON.toJSON(null));
        assertFalse(parsedBean.isPresent());

        Bean bean = new Bean();
        bean.name = "name";
        parsedBean = JSON.fromJSON(Types.optional(Bean.class), JSON.toJSON(Optional.of(bean)));
        assertTrue(parsedBean.isPresent());
        assertEquals(bean.name, parsedBean.get().name);
    }

    @Test
    public void nullObject() {
        String json = JSON.toJSON(null);
        Bean bean = JSON.fromJSON(Bean.class, json);

        assertNull(bean);
    }

    @Test
    public void enumValue() {
        assertEquals(TestEnum.A, JSON.fromEnumValue(TestEnum.class, "A1"));
        assertEquals("B1", JSON.toEnumValue(TestEnum.B));
    }

    enum TestEnum {
        @XmlEnumValue("A1")
        A,
        @XmlEnumValue("B1")
        B
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    static class Bean {
        @XmlElement(name = "attributes")
        public final Map<String, String> attributes = Maps.newHashMap();

        @XmlElement(name = "name")
        public String name;

        @XmlElement(name = "date")
        public LocalDate date;

        @XmlElement(name = "date_time")
        public LocalDateTime dateTime;

        @XmlElement(name = "instant")
        public Instant instant;

        @XmlElement(name = "zoned_date_time")
        public ZonedDateTime zonedDateTime;
    }
}