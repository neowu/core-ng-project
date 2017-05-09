package core.framework.test.search;

import core.framework.api.search.Index;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.time.ZonedDateTime;

/**
 * @author neo
 */
@Index(index = "document", type = "document")
@XmlAccessorType(XmlAccessType.FIELD)
public class TestDocument {
    @XmlElement(name = "id")
    public String id;

    @XmlElement(name = "string_field")
    public String stringField;

    @XmlElement(name = "zoned_date_time_field")
    public ZonedDateTime zonedDateTimeField;
}
