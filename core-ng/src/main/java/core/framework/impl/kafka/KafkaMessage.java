package core.framework.impl.kafka;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.Map;

/**
 * @author neo
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class KafkaMessage<T> {
    @XmlElement(name = "headers")
    public Map<String, String> headers;
    @XmlElement(name = "value")
    public T value;
}
