package core.framework.api.util.json;

import core.framework.api.queue.Message;
import core.framework.api.validate.Length;
import core.framework.api.validate.NotNull;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author neo
 */
@Message(name = "update_product_request")
@XmlAccessorType(XmlAccessType.FIELD)
public class UpdateProductRequest {
    @NotNull
    @XmlElement(name = "product")
    public ProductRequest product = new ProductRequest();

    @NotNull
    @Length(max = 30)
    @XmlElement(name = "requested_by")
    public String requestedBy;
}
