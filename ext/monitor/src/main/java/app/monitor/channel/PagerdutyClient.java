package app.monitor.channel;

import app.monitor.PagerdutyConfig;
import app.monitor.alert.Alert;
import core.framework.api.http.HTTPStatus;
import core.framework.http.ContentType;
import core.framework.http.HTTPClient;
import core.framework.http.HTTPHeaders;
import core.framework.http.HTTPMethod;
import core.framework.http.HTTPRequest;
import core.framework.http.HTTPResponse;
import core.framework.json.JSON;
import core.framework.util.ASCII;
import core.framework.util.Strings;

/**
 * @author ajax
 */
public class PagerdutyClient implements Channel {
    private static final String ACCIDENT_API_URL = "https://api.pagerduty.com/incidents";
    private static final String FROM = "From";
    private final PagerdutyConfig pagerDutyConfig;
    private final HTTPClient httpClient;

    public PagerdutyClient(HTTPClient httpClient, PagerdutyConfig pagerDutyConfig) {
        this.httpClient = httpClient;
        this.pagerDutyConfig = pagerDutyConfig;
    }

    @Override
    public void notify(String serviceId, Alert alert, int alertCountSinceLastSent) {
        PDCreateAccidentAPIRequest createRequest = createRequest(serviceId, alert);
        HTTPRequest request = new HTTPRequest(HTTPMethod.POST, ACCIDENT_API_URL);
        request.headers.put(HTTPHeaders.AUTHORIZATION, "Token token=" + pagerDutyConfig.token);
        request.headers.put(FROM, pagerDutyConfig.from);
        request.body(JSON.toJSON(createRequest), ContentType.APPLICATION_JSON);
        HTTPResponse response = httpClient.execute(request);
        if (response.statusCode != HTTPStatus.CREATED.code) {
            throw new Error(Strings.format("failed to trigger accident to pagerduty, status={}, response={}", response.statusCode, response.text()));
        }
    }

    private PDCreateAccidentAPIRequest createRequest(String serviceId, Alert alert) {
        PDCreateAccidentAPIRequest createRequest = new PDCreateAccidentAPIRequest();
        createRequest.incident = new PDCreateAccidentAPIRequest.Incident();
        createRequest.incident.type = "incident";
        createRequest.incident.title = Strings.format("[{}] {}", ASCII.toUpperCase(alert.app), alert.errorMessage);
        createRequest.incident.service = new PDCreateAccidentAPIRequest.Service();
        createRequest.incident.service.id = serviceId;
        createRequest.incident.service.type = "service_reference";
        if (pagerDutyConfig.priorityId != null) {
            createRequest.incident.priority = new PDCreateAccidentAPIRequest.Priority();
            createRequest.incident.priority.id = pagerDutyConfig.priorityId;
            createRequest.incident.priority.type = "priority_reference";
        }
        createRequest.incident.urgency = "high";
        createRequest.incident.body = new PDCreateAccidentAPIRequest.Body();
        createRequest.incident.body.type = "incident_body";
        createRequest.incident.body.details = details(alert);
        if (pagerDutyConfig.escalationPolicyId != null) {
            createRequest.incident.escalationPolicy = new PDCreateAccidentAPIRequest.EscalationPolicy();
            createRequest.incident.escalationPolicy.id = pagerDutyConfig.escalationPolicyId;
            createRequest.incident.escalationPolicy.type = "escalation_policy_reference";
        }
        return createRequest;
    }

    private String details(Alert alert) {
        var builder = new StringBuilder(256);

        builder.append(alert.severity).append(": ");
        if (alert.site != null) builder.append(alert.site).append(" / ");
        builder.append(alert.app).append('\n');

        if (alert.host != null) builder.append("host: ").append(alert.host).append('\n');
        builder.append("kibana: ").append(alert.docURL()).append('\n');
        if (alert.action != null) builder.append("action: ").append(alert.action).append('\n');

        builder.append("error_code: ").append(alert.errorCode).append("\nmessage: ").append(alert.errorMessage).append('\n');
        return builder.toString();
    }
}
