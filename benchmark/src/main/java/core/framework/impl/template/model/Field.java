package core.framework.impl.template.model;

import javax.xml.bind.annotation.XmlEnumValue;

/**
 * @author neo
 */
public enum Field {
    @XmlEnumValue("CATEGORY")
    CATEGORY,
    @XmlEnumValue("BRAND")
    BRAND,
    @XmlEnumValue("VENDOR")
    VENDOR,
    @XmlEnumValue("COLOR")
    COLOR,
    @XmlEnumValue("SIZE")
    SIZE,
    @XmlEnumValue("PRICE")
    PRICE
}
