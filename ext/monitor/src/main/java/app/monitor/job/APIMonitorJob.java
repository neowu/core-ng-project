package app.monitor.job;

import core.framework.api.http.HTTPStatus;
import core.framework.http.HTTPClient;
import core.framework.http.HTTPMethod;
import core.framework.http.HTTPRequest;
import core.framework.http.HTTPResponse;
import core.framework.internal.log.LogManager;
import core.framework.internal.web.api.APIDefinitionResponse;
import core.framework.json.JSON;
import core.framework.kafka.MessagePublisher;
import core.framework.log.message.StatMessage;
import core.framework.scheduler.Job;
import core.framework.scheduler.JobContext;
import core.framework.util.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public class APIMonitorJob implements Job {
    private final Logger logger = LoggerFactory.getLogger(APIMonitorJob.class);
    private final HTTPClient httpClient;
    private final List<String> serviceURLs;
    private final MessagePublisher<StatMessage> publisher;
    private final Map<String, APIDefinitionResponse> previousDefinitions = Maps.newConcurrentHashMap();

    public APIMonitorJob(HTTPClient httpClient, List<String> serviceURLs, MessagePublisher<StatMessage> publisher) {
        this.httpClient = httpClient;
        this.serviceURLs = serviceURLs;
        this.publisher = publisher;
    }

    @Override
    public void execute(JobContext context) {
        for (String serviceURL : serviceURLs) {
            try {
                checkAPI(serviceURL);
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
                publisher.publish(StatMessageFactory.failedToCollect(LogManager.APP_NAME, null, e));
            }
        }
    }

    private void checkAPI(String serviceURL) {
        HTTPResponse response = httpClient.execute(new HTTPRequest(HTTPMethod.GET, serviceURL + "/_sys/api"));
        if (response.statusCode != HTTPStatus.OK.code) {
            throw new Error("failed to call sys api, statusCode=" + response.statusCode + ", message=" + response.text());
        }
        APIDefinitionResponse currentDefinition = JSON.fromJSON(APIDefinitionResponse.class, response.text());
        APIDefinitionResponse previousDefinition = previousDefinitions.get(currentDefinition.app);
        if (previousDefinition != null) {
            checkAPI(previousDefinition, currentDefinition);
        }
        previousDefinitions.put(currentDefinition.app, currentDefinition);
    }

    private void checkAPI(APIDefinitionResponse previous, APIDefinitionResponse current) {
        var validator = new APIValidator(previous, current);
        String result = validator.validate();
        if (result != null) {
            publishAPIChanged(current.app, result, validator.errorMessage());
        }
    }

    private void publishAPIChanged(String app, String result, String errorMessage) {
        var now = Instant.now();
        var message = new StatMessage();
        message.id = LogManager.ID_GENERATOR.next(now);
        message.date = now;
        message.result = result;
        message.app = app;
        message.errorCode = "API_CHANGED";
        message.errorMessage = errorMessage;
        publisher.publish(message);
    }
}
