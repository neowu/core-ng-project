package core.framework.impl.template.model;

import core.framework.api.validate.Length;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.List;

/**
 * @author neo
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class SearchProductRequest {
    @XmlElement(name = "query")
    public String query;

    @XmlElement(name = "filter")
    public Filter filter = new Filter();

    @XmlElement(name = "skip")
    public Integer skip;

    @XmlElement(name = "limit")
    public Integer limit;

    @Length(min = 1)
    @XmlElementWrapper(name = "aggregations")
    @XmlElement(name = "aggregation")
    public List<Field> aggregations;

    @XmlElement(name = "sort")
    public Sort sort;
}
