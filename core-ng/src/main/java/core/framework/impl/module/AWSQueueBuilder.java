package core.framework.impl.module;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import core.framework.impl.queue.SNSMessagePublisher;
import core.framework.impl.queue.SQSMessageListener;
import core.framework.impl.queue.SQSMessagePublisher;

/**
 * @author neo
 */
// load AWS sdk when needed
public class AWSQueueBuilder {
    private final ModuleContext context;

    public AWSQueueBuilder(ModuleContext context) {
        this.context = context;
    }

    public <T> SQSMessagePublisher<T> sqsPublisher(String queueURL, Class<T> messageClass) {
        return new SQSMessagePublisher<>(amazonSQS(), queueURL, messageClass, context.queueManager.validator(), context.logManager);
    }

    public <T> SNSMessagePublisher<T> snsPublisher(String topicARN, Class<T> messageClass) {
        return new SNSMessagePublisher<>(amazonSNS(), topicARN, messageClass, context.queueManager.validator(), context.logManager);
    }

    public SQSMessageListener listener(String queueURL) {
        SQSMessageListener listener = new SQSMessageListener(context.executor, amazonSQS(), queueURL, context.queueManager.validator(), context.logManager);
        if (!context.test) {
            context.startupHook.add(listener::start);
            context.shutdownHook.add(listener::stop);
        }
        return listener;
    }

    private AmazonSQS amazonSQS() {
        if (context.beanFactory.registered(AmazonSQS.class, null)) {
            return context.beanFactory.bean(AmazonSQS.class, null);
        } else {
            AmazonSQS sqs = new AmazonSQSClient(new DefaultAWSCredentialsProviderChain());
            context.beanFactory.bind(AmazonSQS.class, null, sqs);
            return sqs;
        }
    }

    private AmazonSNS amazonSNS() {
        if (context.beanFactory.registered(AmazonSNS.class, null)) {
            return context.beanFactory.bean(AmazonSNS.class, null);
        } else {
            AmazonSNSClient sns = new AmazonSNSClient(new DefaultAWSCredentialsProviderChain());
            context.beanFactory.bind(AmazonSNS.class, null, sns);
            return sns;
        }
    }
}
