package core.framework.impl.queue;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.SendMessageRequest;
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
public class SQSMessagePublisher<T> implements MessagePublisher<T> {
    private final Logger logger = LoggerFactory.getLogger(SQSMessagePublisher.class);
    private final AmazonSQS sqs;
    private final String defaultQueueURL;
    private final String messageType;
    private final MessageValidator validator;

    public SQSMessagePublisher(AmazonSQS sqs, String defaultQueueURL, Class<?> messageClass, MessageValidator validator) {
        this.sqs = sqs;
        this.defaultQueueURL = defaultQueueURL;
        this.messageType = messageClass.getDeclaredAnnotation(Message.class).name();
        this.validator = validator;
    }

    @Override
    public void publish(T message) {
        publish(defaultQueueURL, message);
    }

    @Override
    public void publish(String queueURL, T message) {
        StopWatch watch = new StopWatch();
        try {
            validator.validate(message);
            SendMessageRequest request = new SendMessageRequest()
                .withQueueUrl(queueURL)
                .withMessageBody(JSON.toJSON(message))
                .addMessageAttributesEntry(SQSMessageListener.MESSAGE_ATTR_PUBLISHER,
                    new MessageAttributeValue().withDataType("String").withStringValue("sqs"))
                .addMessageAttributesEntry(SQSMessageListener.MESSAGE_ATTR_TYPE,
                    new MessageAttributeValue().withDataType("String").withStringValue(messageType))
                .addMessageAttributesEntry(SQSMessageListener.MESSAGE_ATTR_SENDER,
                    new MessageAttributeValue().withDataType("String").withStringValue(Network.localHostName()));

            linkContext(request);

            sqs.sendMessage(request);
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("sqs", elapsedTime);
            logger.debug("publish message, queueURL={}, type={}, elapsedTime={}", queueURL, messageType, elapsedTime);
        }
    }

    private void linkContext(SendMessageRequest request) {
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
