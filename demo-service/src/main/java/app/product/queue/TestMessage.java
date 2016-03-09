package app.product.queue;

import core.framework.api.queue.Message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author neo
 */
@Message(name = "test")
@XmlAccessorType(XmlAccessType.FIELD)
public class TestMessage {
    @XmlElement(name = "name")
    public String name;
}
