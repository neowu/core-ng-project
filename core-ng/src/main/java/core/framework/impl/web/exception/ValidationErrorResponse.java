package core.framework.impl.web.exception;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.Map;

/**
 * @author neo
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ValidationErrorResponse {
    @XmlElement(name = "message")
    public String message;

    @XmlElement(name = "field_errors")
    public Map<String, String> fieldErrors;
}
