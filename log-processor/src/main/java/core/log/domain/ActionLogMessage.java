package core.log.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author neo
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ActionLogMessage {
    public LocalDateTime date;
    public String requestId;
    public String result;
    public Map<String, String> context;
}
