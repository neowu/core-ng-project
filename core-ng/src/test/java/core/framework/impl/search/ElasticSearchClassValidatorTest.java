package core.framework.impl.search;

import org.junit.Test;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public class ElasticSearchClassValidatorTest {
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class TestIndex {
        @XmlElement(name = "date_time_field")
        public LocalDateTime dateTimeField;

        @XmlElement(name = "string_field")
        public String stringField;

        @XmlElement(name = "list_field")
        public List<String> listField;

        @XmlElement(name = "map_field")
        public Map<String, String> mapField;
    }

    @Test
    public void validate() {
        new ElasticSearchClassValidator(TestIndex.class).validate();
    }
}