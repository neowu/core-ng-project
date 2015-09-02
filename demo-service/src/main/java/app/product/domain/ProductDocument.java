package app.product.domain;

import core.framework.api.util.Lists;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.List;

/**
 * @author neo
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ProductDocument {
    @XmlElement(name = "id")
    public Integer id;
    @XmlElement(name = "name")
    public String name;
    @XmlElementWrapper(name = "skus")
    @XmlElement(name = "sku")
    public List<SKUDocument> skus = Lists.newArrayList();
}
