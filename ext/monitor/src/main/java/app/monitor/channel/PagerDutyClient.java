package app.monitor.channel;

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

import java.util.Map;

/**
 * @author ajax
 */
public class PagerDutyClient implements Channel {
    private static final String ACCIDENT_API_URL = "https://api.pagerduty.com/incidents";
    private static final String PRIORITY_ID = "priorityId";
    private static final String ESCALATION_POLICY_ID = "escalationPolicyId";
    private final String token;
    private final String from;
    private final HTTPClient httpClient;

    public PagerDutyClient(HTTPClient httpClient, String token, String from) {
        this.token = token;
        this.from = from;
        this.httpClient = httpClient;
    }

    @Override
    public void notify(String serviceId, Map<String, String> params, Alert alert, int alertCountSinceLastSent) {
        var request = new HTTPRequest(HTTPMethod.POST, ACCIDENT_API_URL);
        request.headers.put(HTTPHeaders.AUTHORIZATION, "Token token=" + token);
        request.headers.put("From", from);
        PagerDutyAccidentAPIRequest pagerDutyRequest = request(serviceId, params, alert);
        request.body(JSON.toJSON(pagerDutyRequest), ContentType.APPLICATION_JSON);
        HTTPResponse response = httpClient.execute(request);
        if (response.statusCode != HTTPStatus.CREATED.code) {
            throw new Error(Strings.format("failed to trigger accident to pagerduty, status={}, response={}", response.statusCode, response.text()));
        }
    }

    private PagerDutyAccidentAPIRequest request(String serviceId, Map<String, String> params, Alert alert) {
        PagerDutyAccidentAPIRequest request = new PagerDutyAccidentAPIRequest();
        request.incident = new PagerDutyAccidentAPIRequest.Incident();
        request.incident.type = "incident";
        request.incident.title = Strings.format("[{}] {}", ASCII.toUpperCase(alert.app), alert.errorMessage);
        request.incident.service = new PagerDutyAccidentAPIRequest.Service();
        request.incident.service.id = serviceId;
        request.incident.service.type = "service_reference";
        String priorityId = params.get(PRIORITY_ID);
        if (priorityId != null) {
            request.incident.priority = new PagerDutyAccidentAPIRequest.Priority();
            request.incident.priority.id = priorityId;
            request.incident.priority.type = "priority_reference";
        }
        request.incident.urgency = "high";
        request.incident.body = new PagerDutyAccidentAPIRequest.Body();
        request.incident.body.type = "incident_body";
        request.incident.body.details = details(alert);
        String escalationPolicyId = params.get(ESCALATION_POLICY_ID);
        if (escalationPolicyId != null) {
            request.incident.escalationPolicy = new PagerDutyAccidentAPIRequest.EscalationPolicy();
            request.incident.escalationPolicy.id = escalationPolicyId;
            request.incident.escalationPolicy.type = "escalation_policy_reference";
        }
        return request;
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
