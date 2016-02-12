package core.framework.api.util.json;

import core.framework.api.validate.NotNull;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author neo
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ProductInventoryView {
    @NotNull
    @XmlElement(name = "warehouse_code")
    public String warehouseCode;

    @NotNull
    @XmlElement(name = "quantity")
    public Integer quantity;
}
