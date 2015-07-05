package core.framework.impl.web.management;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author neo
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class CacheView {
    @XmlElement(name = "name")
    public String name;
    @XmlElement(name = "type")
    public String type;
    @XmlElement(name = "duration")
    public Integer duration;
}
