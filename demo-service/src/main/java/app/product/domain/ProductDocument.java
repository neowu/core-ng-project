package app.product.domain;

import core.framework.api.search.Index;
import core.framework.api.util.Lists;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.List;

/**
 * @author neo
 */
@Index(index = "main", type = "product")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProductDocument {
    @XmlElement(name = "id")
    public Integer id;
    @XmlElement(name = "name")
    public String name;
    @XmlElementWrapper(name = "items")
    @XmlElement(name = "item")
    public List<ProductItemDocument> items = Lists.newArrayList();
}
