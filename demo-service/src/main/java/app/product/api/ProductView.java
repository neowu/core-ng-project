package app.product.api;

import core.framework.api.validate.NotNull;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.time.LocalDateTime;

/**
 * @author neo
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ProductView {
    @XmlElement(name = "id")
    public String id;

    @NotNull(message = "name is required")
    @XmlElement(name = "name")
    public String name;

    @XmlElement(name = "desc")
    public String description;

    @XmlElement(name = "created_time")
    public LocalDateTime createdTime;
}
