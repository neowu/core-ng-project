package core.framework.api.util.json;

import core.framework.api.validate.Length;
import core.framework.api.validate.Min;
import core.framework.api.validate.NotNull;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ProductRequest {
    @NotNull
    @Length(max = 30)
    @XmlElement(name = "vendor_number")
    public String vendorNumber;

    @NotNull
    @Length(max = 100)
    @XmlElement(name = "vendor_sku")
    public String sku;

    @XmlElement(name = "listing_status")
    public String listingStatus;

    @Length(min = 1, max = 100)
    @XmlElement(name = "parent_vendor_sku")
    public String parentSKU;

    @Length(max = 500)
    @XmlElement(name = "name")
    public String name;

    @Length(max = 5000)
    @XmlElement(name = "description")
    public String description;

    @Length(max = 50)
    @XmlElement(name = "brand_name")
    public String brandName;

    @Length(max = 500)
    @XmlElement(name = "category_id")
    public String categoryId;

    @Length(max = 10)
    @XmlElement(name = "external_product_id_type")
    public String externalProductIdType;

    @Length(max = 30)
    @XmlElement(name = "external_product_id")
    public String externalProductId;

    @Length(max = 100)
    @XmlElement(name = "type")
    public String type;

    @XmlElement(name = "unit_cost")
    public BigDecimal unitCost;

    @XmlElement(name = "list_price")
    public BigDecimal listPrice;

    @XmlElement(name = "map_price")
    public BigDecimal mapPrice;

    @Min(0.01)
    @XmlElement(name = "standard_price")
    public BigDecimal standardPrice;

    @XmlElement(name = "sale_price")
    public BigDecimal salePrice;

    @XmlElement(name = "sale_start_date")
    public LocalDateTime saleStartDate;

    @XmlElement(name = "sale_end_date")
    public LocalDateTime saleEndDate;

    @Min(0.01)
    @XmlElement(name = "weight")
    public BigDecimal weight;

    @XmlElement(name = "harmonized_code")
    public String harmonizedCode;

    @XmlElement(name = "on_hold")
    public Boolean onHold;

    @XmlElementWrapper(name = "eligible_country_codes")
    @XmlElement(name = "eligible_country_code")
    public List<String> eligibleCountryCodes;

    @XmlElement(name = "attributes")
    public Map<String, String> attributes;

    @XmlElementWrapper(name = "inventories")
    @XmlElement(name = "inventory")
    public List<ProductInventoryView> inventories;
}
