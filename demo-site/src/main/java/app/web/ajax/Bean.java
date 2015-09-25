package app.web.ajax;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author neo
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Bean {
    @XmlElement(name = "name")
    public String name;
    @XmlElement(name = "desc")
    public String description;
}
