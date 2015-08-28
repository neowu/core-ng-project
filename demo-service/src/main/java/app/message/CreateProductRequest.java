package app.message;

import core.framework.api.queue.Message;
import core.framework.api.validate.NotNull;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author neo
 */
@Message(name = "product_request")
@XmlAccessorType(XmlAccessType.FIELD)
public class CreateProductRequest {
    @XmlElement(name = "id")
    public Integer id;
    @NotNull(message = "name is required")
    @XmlElement(name = "name")
    public String name;

    @XmlElement(name = "finish")
    public Boolean finish;
}
