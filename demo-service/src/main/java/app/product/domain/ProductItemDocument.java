package app.product.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author neo
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ProductItemDocument {
    @XmlElement(name = "sku")
    public String sku;
    @XmlElement(name = "name")
    public String name;
    @XmlElement(name = "price")
    public Double price;
}
