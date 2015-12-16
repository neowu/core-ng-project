package core.framework.impl.log.queue;

import core.framework.api.queue.Message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.List;

/**
 * @author neo
 */
@Message(name = "action_logs")
@XmlAccessorType(XmlAccessType.FIELD)
public class ActionLogMessages {
    @XmlElementWrapper(name = "logs")
    @XmlElement(name = "log")
    public List<ActionLogMessage> logs;
}
