package app.product.web;

import core.framework.api.validate.NotNull;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * @author neo
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ProductView {
    @XmlElement(name = "id")
    public Integer id;
    @XmlElement(name = "date")
    public LocalDateTime date;
    @XmlElement(name = "instant")
    public Instant instant;
    @NotNull(message = "name is required")
    @XmlElement(name = "name")
    public String name;
    @NotNull(message = "price is required")
    @XmlElement(name = "price")
    public Double price;
    @XmlElement(name = "desc")
    public String description;
}
