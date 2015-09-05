package core.framework.impl.web;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnumValue;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class TestBean {
    @XmlElement(name = "date_time_field")
    public LocalDateTime dateTimeField;

    @XmlElement(name = "string_field")
    public String stringField;

    @XmlElement(name = "list_field")
    public List<String> listField;

    @XmlElement(name = "map_field")
    public Map<String, String> mapField;

    @XmlElement(name = "child_field")
    public Child childField;

    @XmlElement(name = "children_field")
    public List<Child> childrenField;

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Child {
        @XmlElement(name = "boolean_field")
        public String booleanField;
    }

    @XmlElement(name = "enum_field")
    public TestEnum enumField;

    public enum TestEnum {
        @XmlEnumValue("V1")
        VALUE1,
        @XmlEnumValue("V2")
        VALUE2
    }
}
