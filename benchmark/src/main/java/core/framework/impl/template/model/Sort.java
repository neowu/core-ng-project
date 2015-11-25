package core.framework.impl.template.model;

import javax.xml.bind.annotation.XmlEnumValue;

/**
 * @author neo
 */
public enum Sort {
    @XmlEnumValue("PRICE_ASC")
    PRICE_ASC,
    @XmlEnumValue("PRICE_DESC")
    PRICE_DESC,
    @XmlEnumValue("NEWEST")
    NEWEST,
    @XmlEnumValue("RELEVANCE")
    RELEVANCE
}
