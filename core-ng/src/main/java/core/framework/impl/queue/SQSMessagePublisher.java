package core.framework.impl.queue;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.SendMessageRequest;
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
public class SQSMessagePublisher<T> implements MessagePublisher<T> {
    private final Logger logger = LoggerFactory.getLogger(SQSMessagePublisher.class);
    private final AmazonSQS sqs;
    private final String queueURL;
    private final String messageType;
    private final MessageValidator validator;
    private final LogManager logManager;

    public SQSMessagePublisher(AmazonSQS sqs, String queueURL, Class<?> messageClass, MessageValidator validator, LogManager logManager) {
        this.sqs = sqs;
        this.queueURL = queueURL;
        this.messageType = messageClass.getDeclaredAnnotation(Message.class).name();
        this.validator = validator;
        this.logManager = logManager;
    }

    @Override
    public void publish(T message) {
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
                .addMessageAttributesEntry(SQSMessageListener.MESSAGE_ATTR_CLIENT_IP,
                    new MessageAttributeValue().withDataType("String").withStringValue(Network.localHostAddress()));

            linkContext(request);

            sqs.sendMessage(request);
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("sqs", elapsedTime);
            logger.debug("publish message, queueURL={}, type={}, elapsedTime={}", queueURL, messageType, elapsedTime);
        }
    }

    @Override
    public void publish(String routingKey, T message) {
        throw new Error("sqs message publisher does not support publishing with routingKey");
    }

    private void linkContext(SendMessageRequest request) {
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
