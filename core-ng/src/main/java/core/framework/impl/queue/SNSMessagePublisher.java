package core.framework.impl.queue;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import core.framework.api.log.ActionLogContext;
import core.framework.api.queue.Message;
import core.framework.api.queue.MessagePublisher;
import core.framework.api.util.JSON;
import core.framework.api.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author neo
 */
public class SNSMessagePublisher<T> implements MessagePublisher<T> {
    private final Logger logger = LoggerFactory.getLogger(SNSMessagePublisher.class);
    private final AmazonSNS sns;
    private final String defaultTopicARN;
    private final String messageType;
    private final MessageValidator validator;

    public SNSMessagePublisher(AmazonSNS sns, String defaultTopicARN, Class<?> messageClass, MessageValidator validator) {
        this.sns = sns;
        this.defaultTopicARN = defaultTopicARN;
        this.messageType = messageClass.getDeclaredAnnotation(Message.class).name();
        this.validator = validator;
    }

    @Override
    public void publish(T message) {
        publish(defaultTopicARN, message);
    }

    @Override
    public void publish(String topicARN, T message) {
        StopWatch watch = new StopWatch();
        try {
            validator.validate(message);
            PublishRequest request = new PublishRequest(topicARN, JSON.toJSON(message), messageType)
                .addMessageAttributesEntry(SQSMessageListener.MESSAGE_ATTR_PUBLISHER,
                    new MessageAttributeValue().withDataType("String").withStringValue("sns"))
                .addMessageAttributesEntry(SQSMessageListener.MESSAGE_ATTR_TYPE,
                    new MessageAttributeValue().withDataType("String").withStringValue(messageType))
                .addMessageAttributesEntry(SQSMessageListener.MESSAGE_ATTR_SENDER,
                    new MessageAttributeValue().withDataType("String").withStringValue(Network.localHostName()));

            linkContext(request);

            sns.publish(request);
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("sns", elapsedTime);
            logger.debug("publish message, topicARN={}, type={}, elapsedTime={}", topicARN, messageType, elapsedTime);
        }
    }

    private void linkContext(PublishRequest request) {
        ActionLogContext.get(ActionLogContext.REQUEST_ID).ifPresent(requestId ->
            request.addMessageAttributesEntry(SQSMessageListener.MESSAGE_ATTR_REQUEST_ID,
                new MessageAttributeValue().withDataType("String").withStringValue(requestId)));

        ActionLogContext.get(ActionLogContext.TRACE).ifPresent(trace -> {
            if ("true".equals(trace))
                request.addMessageAttributesEntry(SQSMessageListener.MESSAGE_ATTR_TRACE,
                    new MessageAttributeValue().withDataType("String").withStringValue("true"));
        });
    }
}
