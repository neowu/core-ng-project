package app.monitor.job;

import core.framework.api.http.HTTPStatus;
import core.framework.http.HTTPClient;
import core.framework.http.HTTPMethod;
import core.framework.http.HTTPRequest;
import core.framework.http.HTTPResponse;
import core.framework.internal.log.LogManager;
import core.framework.internal.web.api.APIDefinitionV2Response;
import core.framework.json.JSON;
import core.framework.kafka.MessagePublisher;
import core.framework.log.message.StatMessage;
import core.framework.scheduler.Job;
import core.framework.scheduler.JobContext;
import core.framework.util.Exceptions;
import core.framework.util.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;

/**
 * @author neo
 */
public class APIMonitorJob implements Job {
    private final Logger logger = LoggerFactory.getLogger(APIMonitorJob.class);
    private final HTTPClient httpClient = HTTPClient.builder().userAgent("monitor").trustAll().build();
    private final Map<String, String> serviceURLs;
    private final MessagePublisher<StatMessage> publisher;
    private final Map<String, APIDefinitionV2Response> previousDefinitions = Maps.newConcurrentHashMap();

    public APIMonitorJob(Map<String, String> serviceURLs, MessagePublisher<StatMessage> publisher) {
        this.serviceURLs = serviceURLs;
        this.publisher = publisher;
    }

    @Override
    public void execute(JobContext context) {
        for (Map.Entry<String, String> entry : serviceURLs.entrySet()) {
            String app = entry.getKey();
            String serviceURL = entry.getValue();
            try {
                checkAPI(app, serviceURL);
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
                publishError(app, e);
            }
        }
    }

    private void checkAPI(String app, String serviceURL) {
        HTTPResponse response = httpClient.execute(new HTTPRequest(HTTPMethod.GET, serviceURL + "/_sys/api/v2"));
        if (response.statusCode != HTTPStatus.OK.code) {
            throw new Error("failed to call sys api, statusCode=" + response.statusCode + ", message=" + response.text());
        }
        APIDefinitionV2Response currentDefinition = JSON.fromJSON(APIDefinitionV2Response.class, response.text());
        APIDefinitionV2Response previousDefinition = previousDefinitions.get(app);
        if (previousDefinition != null) {
            checkAPI(app, previousDefinition, currentDefinition);
        }
        previousDefinitions.put(app, currentDefinition);
    }

    private void checkAPI(String app, APIDefinitionV2Response previous, APIDefinitionV2Response current) {
        var validator = new APIValidator(previous, current);
        String result = validator.validate();
        if (result != null) {
            publishAPIChanged(app, result, validator.errorMessage());
        }
    }

    private void publishAPIChanged(String app, String result, String errorMessage) {
        var message = new StatMessage();
        Instant now = Instant.now();
        message.id = LogManager.ID_GENERATOR.next(now);
        message.date = Instant.now();
        message.result = result;
        message.app = app;
        message.errorCode = "API_CHANGED";
        message.errorMessage = errorMessage;
        publisher.publish(message);
    }

    private void publishError(String app, Throwable e) {
        var message = new StatMessage();
        Instant now = Instant.now();
        message.id = LogManager.ID_GENERATOR.next(now);
        message.date = now;
        message.result = "ERROR";
        message.app = app;
        message.errorCode = "FAILED_TO_COLLECT";
        message.errorMessage = e.getMessage();
        message.info = Map.of("stack_trace", Exceptions.stackTrace(e));
        publisher.publish(message);
    }
}
