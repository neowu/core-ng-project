package app.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author neo
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ProductIndex {
    @XmlElement(name = "id")
    public Integer id;
    @XmlElement(name = "name")
    public String name;
}
