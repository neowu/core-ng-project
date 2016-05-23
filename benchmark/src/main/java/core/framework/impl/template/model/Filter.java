package core.framework.impl.template.model;

import core.framework.api.validate.Length;
import core.framework.api.validate.NotEmpty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.List;

/**
 * @author neo
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Filter {
    @XmlElement(name = "country_code")
    public String countryCode;

    @Length(min = 1)
    @XmlElementWrapper(name = "vendor_numbers")
    @XmlElement(name = "vendor_number")
    public List<String> vendorNumbers;

    @XmlElement(name = "tag")
    public String tag;

    @XmlElement(name = "category_id")
    public String categoryId;

    @Length(min = 1)
    @NotEmpty
    @XmlElementWrapper(name = "brands")
    @XmlElement(name = "brand")
    public List<String> brands;

    @Length(min = 1)
    @NotEmpty
    @XmlElementWrapper(name = "colors")
    @XmlElement(name = "color")
    public List<String> colors;

    @Length(min = 1)
    @NotEmpty
    @XmlElementWrapper(name = "sizes")
    @XmlElement(name = "size")
    public List<String> sizes;

    @XmlElement(name = "min_price")
    public Double minPrice;

    @XmlElement(name = "max_price")
    public Double maxPrice;
}
