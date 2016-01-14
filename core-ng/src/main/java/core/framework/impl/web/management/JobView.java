package core.framework.impl.web.management;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author neo
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class JobView {
    @XmlElement(name = "name")
    public String name;
    @XmlElement(name = "job_class")
    public String jobClass;
    @XmlElement(name = "frequency")
    public String frequency;
}
