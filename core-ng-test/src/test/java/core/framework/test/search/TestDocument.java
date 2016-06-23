package core.framework.test.search;

import core.framework.api.search.Index;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author neo
 */
@Index(index = "document", type = "document")
@XmlAccessorType(XmlAccessType.FIELD)
public class TestDocument {
    @XmlElement(name = "string_field")
    public String stringField;
}
