package core.framework.impl.queue;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import core.framework.api.log.ActionLogContext;
import core.framework.api.queue.Message;
import core.framework.api.queue.MessagePublisher;
import core.framework.api.util.JSON;
import core.framework.api.util.StopWatch;
import core.framework.impl.log.ActionLog;
import core.framework.impl.log.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author neo
 */
public class SNSMessagePublisher<T> implements MessagePublisher<T> {
    private final Logger logger = LoggerFactory.getLogger(SNSMessagePublisher.class);
    private final AmazonSNS sns;
    private final String topicARN;
    private final String messageType;
    private final MessageValidator validator;
    private final LogManager logManager;

    public SNSMessagePublisher(AmazonSNS sns, String topicARN, Class<?> messageClass, MessageValidator validator, LogManager logManager) {
        this.sns = sns;
        this.topicARN = topicARN;
        this.messageType = messageClass.getDeclaredAnnotation(Message.class).name();
        this.validator = validator;
        this.logManager = logManager;
    }

    @Override
    public void publish(T message) {
        StopWatch watch = new StopWatch();
        try {
            validator.validate(message);
            PublishRequest request = new PublishRequest(topicARN, JSON.toJSON(message), messageType)
                .addMessageAttributesEntry(SQSMessageListener.MESSAGE_ATTR_PUBLISHER,
                    new MessageAttributeValue().withDataType("String").withStringValue("sns"))
                .addMessageAttributesEntry(SQSMessageListener.MESSAGE_ATTR_TYPE,
                    new MessageAttributeValue().withDataType("String").withStringValue(messageType))
                .addMessageAttributesEntry(SQSMessageListener.MESSAGE_ATTR_CLIENT_IP,
                    new MessageAttributeValue().withDataType("String").withStringValue(Network.localHostAddress()));

            linkContext(request);

            sns.publish(request);
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("sns", elapsedTime);
            logger.debug("publish message, topicARN={}, type={}, elapsedTime={}", topicARN, messageType, elapsedTime);
        }
    }

    @Override
    public void publish(String routingKey, T message) {
        throw new Error("sns message publisher does not support publishing with routingKey");
    }

    private void linkContext(PublishRequest request) {
        ActionLog actionLog = logManager.currentActionLog();
        if (actionLog == null) return;

        request.addMessageAttributesEntry(SQSMessageListener.MESSAGE_ATTR_REF_ID,
            new MessageAttributeValue().withDataType("String").withStringValue(actionLog.refId()));

        if (actionLog.trace) {
            request.addMessageAttributesEntry(SQSMessageListener.MESSAGE_ATTR_TRACE,
                new MessageAttributeValue().withDataType("String").withStringValue("true"));
        }
    }
}
