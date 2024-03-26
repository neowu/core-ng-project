package core.framework.internal.kafka;

import core.framework.internal.async.ThreadPools;
import core.framework.internal.async.VirtualThread;
import core.framework.internal.json.JSONReader;
import core.framework.internal.log.ActionLog;
import core.framework.internal.log.LogManager;
import core.framework.internal.log.PerformanceWarning;
import core.framework.internal.log.filter.BytesLogParam;
import core.framework.kafka.Message;
import core.framework.util.Sets;
import core.framework.util.StopWatch;
import core.framework.util.Threads;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static core.framework.log.Markers.errorCode;

/**
 * @author neo
 */
class MessageListenerThread extends Thread {
    private final Logger logger = LoggerFactory.getLogger(MessageListenerThread.class);
    private final MessageListener listener;
    private final LogManager logManager;

    private final Consumer<String, byte[]> consumer;
    private final Builder.OfVirtual thread;

    private final Semaphore semaphore;
    private final int concurrency;

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notProcessing = lock.newCondition();
    private boolean processing;

    private volatile boolean shutdown;

    MessageListenerThread(String name, Consumer<String, byte[]> consumer, MessageListener listener) {
        super(name);
        this.consumer = consumer;
        this.listener = listener;
        logManager = listener.logManager;
        concurrency = listener.concurrency;
        semaphore = new Semaphore(concurrency);
        thread = ThreadPools.virtualThreadBuilder(name + "-");   // used in single thread, no need to use factory()
    }

    @SuppressWarnings("PMD.UnusedAssignment")   // false positive
    @Override
    public void run() {
        try {
            processing = true;
            process();
        } finally {
            processing = false;
            signal();
        }
    }

    private void signal() {
        lock.lock();
        try {
            notProcessing.signalAll();
        } finally {
            lock.unlock();
        }
    }

    private void process() {
        while (!shutdown) {
            try {
                Collection<KafkaMessages> allMessages = poll();   // consumer should call poll at least once every MAX_POLL_INTERVAL_MS
                if (allMessages == null) continue;

                processAll(allMessages);
            } catch (Throwable e) {
                if (!shutdown) {
                    logger.error("failed to poll messages, retry in 10 seconds", e);
                    Threads.sleepRoughly(Duration.ofSeconds(10));
                }
            }
        }

        logger.info("close kafka consumer, name={}", getName());
        consumer.close();
    }

    @Nullable
    Collection<KafkaMessages> poll() {
        ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofSeconds(30));
        if (records.isEmpty()) return null;

        Map<String, KafkaMessages> messageMappings = new LinkedHashMap<>();
        for (ConsumerRecord<String, byte[]> record : records) {
            String topic = record.topic();
            KafkaMessages messages = messageMappings.computeIfAbsent(topic, KafkaMessages::new);
            if (listener.bulkProcesses.containsKey(topic)) {
                messages.addUnordered(record);  // bulk is processed in single thread
                messages.bulk = true;
            } else {
                messages.addOrdered(record);
            }
        }
        return messageMappings.values();
    }

    void shutdown() {
        shutdown = true;
        // do not call interrupt(), it will interrupt consumer coordinator,
        // the only downside of not calling interrupt() is if thread is at process->exception->sleepRoughly, shutdown will have to wait until sleep ends
        consumer.wakeup();
    }

    boolean awaitTermination(long timeoutInMs) throws InterruptedException {
        long end = System.currentTimeMillis() + timeoutInMs;
        lock.lock();
        try {
            while (processing) {
                long left = end - System.currentTimeMillis();
                if (left <= 0) {
                    return false;
                }
                notProcessing.await(left, TimeUnit.MILLISECONDS);
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

    void processAll(Collection<KafkaMessages> allMessages) throws InterruptedException {
        var watch = new StopWatch();
        int count = 0;
        int size = 0;

        for (KafkaMessages messages : allMessages) {
            count += messages.count;
            size += messages.size;

            if (messages.bulk) {
                MessageProcess<?> bulkProcess = listener.bulkProcesses.get(messages.topic);
                processBulk(bulkProcess, messages);
            } else {
                MessageProcess<?> process = listener.processes.get(messages.topic);
                processSingle(process, messages);
            }
        }

        semaphore.acquire(concurrency);
        try {
            consumer.commitAsync();
        } finally {
            semaphore.release(concurrency);
        }

        logger.info("process kafka messages, count={}, size={}, elapsed={}", count, size, watch.elapsed());
    }

    private void processSingle(MessageProcess<?> process, KafkaMessages messages) throws InterruptedException {
        for (KafkaMessage message : messages.unordered) {
            semaphore.acquire();
            thread.start(() -> {
                try {
                    VirtualThread.COUNT.increase();
                    handleSingle(messages.topic, process, message);
                } finally {
                    VirtualThread.COUNT.decrease();
                    semaphore.release();
                }
            });
        }
        for (KafkaMessage message : messages.ordered.values()) {
            semaphore.acquire();
            thread.start(() -> {
                try {
                    VirtualThread.COUNT.increase();
                    handleSingle(messages.topic, process, message);
                    if (message.subsequent != null) {
                        for (KafkaMessage subsequent : message.subsequent) {
                            handleSingle(messages.topic, process, subsequent);
                        }
                    }
                } finally {
                    VirtualThread.COUNT.decrease();
                    semaphore.release();
                }
            });
        }
    }

    <T> void handleSingle(String topic, MessageProcess<T> process, KafkaMessage message) {
        ActionLog actionLog = logManager.begin("=== message handling begin ===", null);
        try {
            initAction(actionLog, topic, process.handler.getClass().getCanonicalName(), process.warnings);

            actionLog.track("kafka", 0, 1, 0);

            if (message.trace != null) actionLog.trace = message.trace;
            if (message.correlationId != null) actionLog.correlationIds = List.of(message.correlationId);
            if (message.client != null) actionLog.clients = List.of(message.client);
            if (message.refId != null) actionLog.refIds = List.of(message.refId);
            logger.debug("[header] refId={}, client={}, correlationId={}, trace={}", message.refId, message.client, message.correlationId, message.trace);

            actionLog.context.put("key", Collections.singletonList(message.key)); // key can be null

            checkConsumerDelay(actionLog, message.timestamp, listener.longConsumerDelayThresholdInNano);

            logger.debug("[message] key={}, value={}, timestamp={}", message.key, new BytesLogParam(message.value), message.timestamp);

            T messageObject = process.reader.fromJSON(message.value);
            process.validator.validate(messageObject, false);
            process.handler().handle(message.key, messageObject);
        } catch (Throwable e) {
            logManager.logError(e);
        } finally {
            logManager.end("=== message handling end ===");
        }
    }

    private void processBulk(MessageProcess<?> bulkProcess, KafkaMessages messages) throws InterruptedException {
        semaphore.acquire();
        thread.start(() -> {
            VirtualThread.COUNT.increase();
            try {
                handleBulk(messages.topic, bulkProcess, messages.unordered);
            } finally {
                VirtualThread.COUNT.decrease();
                semaphore.release();
            }
        });
    }

    <T> void handleBulk(String topic, MessageProcess<T> process, List<KafkaMessage> messages) {
        ActionLog actionLog = logManager.begin("=== message handling begin ===", null);
        try {
            initAction(actionLog, topic, process.handler.getClass().getCanonicalName(), process.warnings);

            List<Message<T>> messageObjects = messages(messages, actionLog, process.reader);
            for (Message<T> message : messageObjects) {   // validate after fromJSON, so it can track refId/correlationId
                process.validator.validate(message.value, false);
            }

            process.bulkHandler().handle(messageObjects);
        } catch (Throwable e) {
            logManager.logError(e);
        } finally {
            logManager.end("=== message handling end ===");
        }
    }

    private void initAction(ActionLog actionLog, String topic, String handler, PerformanceWarning[] warnings) {
        actionLog.action("topic:" + topic);
        actionLog.warningContext.maxProcessTimeInNano(listener.maxProcessTimeInNano);
        actionLog.context.put("topic", List.of(topic));
        actionLog.context.put("handler", List.of(handler));
        logger.debug("topic={}, handler={}", topic, handler);
        if (warnings != null) actionLog.initializeWarnings(warnings);
    }

    <T> List<Message<T>> messages(List<KafkaMessage> messages, ActionLog actionLog, JSONReader<T> reader) throws IOException {
        int size = messages.size();
        actionLog.track("kafka", 0, size, 0);
        List<Message<T>> messageObjects = new ArrayList<>(size);
        Set<String> correlationIds = new HashSet<>();
        Set<String> clients = new HashSet<>();
        Set<String> refIds = new HashSet<>();
        Set<String> keys = Sets.newHashSetWithExpectedSize(size);
        long minTimestamp = Long.MAX_VALUE;

        for (KafkaMessage message : messages) {
            if (message.trace != null) actionLog.trace = message.trace;   // trigger trace if any message is trace
            if (message.correlationId != null) correlationIds.add(message.correlationId);
            if (message.client != null) clients.add(message.client);
            if (message.refId != null) refIds.add(message.refId);
            keys.add(message.key);

            logger.debug("[message] key={}, value={}, timestamp={}, refId={}, client={}, correlationId={}, trace={}",
                message.key, new BytesLogParam(message.value), message.timestamp, message.refId, message.client, message.correlationId, message.trace);

            if (minTimestamp > message.timestamp) minTimestamp = message.timestamp;

            T messageObject = reader.fromJSON(message.value);
            messageObjects.add(new Message<>(message.key, messageObject));
        }
        actionLog.context.put("key", new ArrayList<>(keys));    // keys could contain null

        if (!correlationIds.isEmpty()) actionLog.correlationIds = List.copyOf(correlationIds);  // action log kafka appender doesn't send headers
        if (!clients.isEmpty()) actionLog.clients = List.copyOf(clients);
        if (!refIds.isEmpty()) actionLog.refIds = List.copyOf(refIds);
        checkConsumerDelay(actionLog, minTimestamp, listener.longConsumerDelayThresholdInNano);
        return messageObjects;
    }

    void checkConsumerDelay(ActionLog actionLog, long timestamp, long longConsumerDelayThresholdInNano) {
        long delay = (actionLog.date.toEpochMilli() - timestamp) * 1_000_000;     // convert to nanoseconds
        logger.debug("consumerDelay={}", Duration.ofNanos(delay));
        actionLog.stats.put("consumer_delay", (double) delay);
        // refer to core.framework.internal.kafka.MessageListener.createConsumer, MAX_POLL_INTERVAL_MS_CONFIG = 30 mins
        // log as error if delay > 15 mins
        if (delay > 900_000_000_000L) {
            logger.error(errorCode("LONG_CONSUMER_DELAY"), "consumer delay is too long, delay={}", Duration.ofNanos(delay));
        } else if (delay > longConsumerDelayThresholdInNano) {
            logger.warn(errorCode("LONG_CONSUMER_DELAY"), "consumer delay is too long, delay={}", Duration.ofNanos(delay));
        }
    }
}
