package core.framework.impl.queue;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author neo
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class SNSMessage {
    @XmlElement(name = "Type")
    public String type;
    @XmlElement(name = "MessageId")
    public String messageId;
    @XmlElement(name = "TopicArn")
    public String topicARN;
    @XmlElement(name = "Subject")
    public String subject;
    @XmlElement(name = "Message")
    public String message;
    @XmlElement(name = "Timestamp")
    public String timestamp;
    @XmlElement(name = "MessageAttributes")
    public SNSMessageAttributes attributes = new SNSMessageAttributes();

    public static class SNSMessageAttributes {
        @XmlElement(name = SQSMessageListener.MESSAGE_ATTR_CLIENT_IP)
        public SNSMessageAttributeValue clientIP;

        @XmlElement(name = SQSMessageListener.MESSAGE_ATTR_TYPE)
        public SNSMessageAttributeValue eventType;

        @XmlElement(name = SQSMessageListener.MESSAGE_ATTR_PUBLISHER)
        public SNSMessageAttributeValue eventPublisher;

        @XmlElement(name = SQSMessageListener.MESSAGE_ATTR_REF_ID)
        public SNSMessageAttributeValue refId;

        @XmlElement(name = SQSMessageListener.MESSAGE_ATTR_TRACE)
        public SNSMessageAttributeValue trace;
    }

    public static class SNSMessageAttributeValue {
        @XmlElement(name = "Type")
        public String type;
        @XmlElement(name = "Value")
        public String value;
    }
}
