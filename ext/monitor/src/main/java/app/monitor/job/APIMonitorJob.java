package app.monitor.job;

import app.monitor.api.APIValidator;
import app.monitor.api.APIWarnings;
import app.monitor.api.MessageAPIValidator;
import core.framework.api.http.HTTPStatus;
import core.framework.http.HTTPClient;
import core.framework.http.HTTPMethod;
import core.framework.http.HTTPRequest;
import core.framework.http.HTTPResponse;
import core.framework.internal.log.LogManager;
import core.framework.internal.web.api.APIDefinitionResponse;
import core.framework.internal.web.api.MessageAPIDefinitionResponse;
import core.framework.json.JSON;
import core.framework.kafka.MessagePublisher;
import core.framework.log.message.StatMessage;
import core.framework.scheduler.Job;
import core.framework.scheduler.JobContext;
import core.framework.util.Maps;
import core.framework.util.Strings;
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
    private final Map<String, APIDefinitionResponse> previousAPIDefinitions = Maps.newConcurrentHashMap();
    private final Map<String, MessageAPIDefinitionResponse> previousMessageDefinitions = Maps.newConcurrentHashMap();

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
                checkMessageAPI(serviceURL);
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
        APIDefinitionResponse previousDefinition = previousAPIDefinitions.get(currentDefinition.app);
        if (previousDefinition != null && !Strings.equals(previousDefinition.version, currentDefinition.version)) {
            var validator = new APIValidator(previousDefinition, currentDefinition);
            APIWarnings warnings = validator.validate();
            publishWarnings(warnings, currentDefinition.app, "API_CHANGED");
        }
        previousAPIDefinitions.put(currentDefinition.app, currentDefinition);
    }

    private void checkMessageAPI(String serviceURL) {
        HTTPResponse response = httpClient.execute(new HTTPRequest(HTTPMethod.GET, serviceURL + "/_sys/api/message"));
        if (response.statusCode != HTTPStatus.OK.code) {
            throw new Error("failed to call sys api, statusCode=" + response.statusCode + ", message=" + response.text());
        }
        MessageAPIDefinitionResponse currentDefinition = JSON.fromJSON(MessageAPIDefinitionResponse.class, response.text());
        MessageAPIDefinitionResponse previousDefinition = previousMessageDefinitions.get(currentDefinition.app);
        if (previousDefinition != null && !Strings.equals(previousDefinition.version, currentDefinition.version)) {
            var validator = new MessageAPIValidator(previousDefinition, currentDefinition);
            APIWarnings warnings = validator.validate();
            publishWarnings(warnings, currentDefinition.app, "MESSAGE_API_CHANGED");
        }
        previousMessageDefinitions.put(currentDefinition.app, currentDefinition);
    }

    private void publishWarnings(APIWarnings warnings, String app, String errorCode) {
        String result = warnings.result();
        if (result != null) {
            var now = Instant.now();
            var message = new StatMessage();
            message.id = LogManager.ID_GENERATOR.next(now);
            message.date = now;
            message.result = result;
            message.app = app;
            message.errorCode = errorCode;
            message.errorMessage = warnings.errorMessage();
            publisher.publish(message);
        }
    }
}
