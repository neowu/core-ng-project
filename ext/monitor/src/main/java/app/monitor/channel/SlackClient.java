package app.monitor.channel;

import app.monitor.alert.Alert;
import core.framework.http.ContentType;
import core.framework.http.HTTPClient;
import core.framework.http.HTTPHeaders;
import core.framework.http.HTTPMethod;
import core.framework.http.HTTPRequest;
import core.framework.http.HTTPResponse;
import core.framework.json.JSON;
import core.framework.log.Severity;
import core.framework.util.Strings;

import java.time.LocalDateTime;
import java.time.temporal.IsoFields;
import java.util.List;
import java.util.Map;

/**
 * @author ericchung
 */
public class SlackClient implements Channel {
    private static final String SLACK_API_URL = "https://slack.com/api/chat.postMessage";
    private final String[][] colors = {
            {"#ff5c33", "#ff9933"}, // 2 colors for warn, change color for weekly review of every week
            {"#a30101", "#e62a00"}  // 2 colors for error
    };
    private final String token;
    private final HTTPClient httpClient;

    public SlackClient(HTTPClient httpClient, String token) {
        this.httpClient = httpClient;
        this.token = token;
    }

    @Override
    public void notify(String channel, Map<String, String> params, Alert alert, int alertCountSinceLastSent) {
        String message = message(alert, alertCountSinceLastSent);
        String color = color(alert.severity, alert.date);
        send(channel, message, color);
    }

    // refer to https://api.slack.com/methods/chat.postMessage
    void send(String channel, String message, String color) {
        SlackMessageAPIRequest request = request(channel, message, color);

        var httpRequest = new HTTPRequest(HTTPMethod.POST, SLACK_API_URL);
        httpRequest.headers.put(HTTPHeaders.AUTHORIZATION, "Bearer " + token);
        httpRequest.body(JSON.toJSON(request), ContentType.APPLICATION_JSON);
        HTTPResponse httpResponse = httpClient.execute(httpRequest);

        String responseBody = httpResponse.text();
        if (httpResponse.statusCode != 200)
            throw new Error(Strings.format("failed to send message to slack, status={}, response={}", httpResponse.statusCode, responseBody));

        SlackMessageAPIResponse response = JSON.fromJSON(SlackMessageAPIResponse.class, responseBody);
        if (!Boolean.TRUE.equals(response.ok)) {
            throw new Error("failed to send message to slack, response=" + responseBody);
        }
    }

    SlackMessageAPIRequest request(String channel, String message, String color) {
        var request = new SlackMessageAPIRequest();
        request.channel = channel;
        var attachment = new SlackMessageAPIRequest.Attachment();
        attachment.color = color;
        attachment.text = message;
        request.attachments = List.of(attachment);
        return request;
    }

    String message(Alert alert, int alertCountSinceLastSent) {
        var builder = new StringBuilder(256);
        if (alertCountSinceLastSent > 0) builder.append("*[").append(alertCountSinceLastSent).append("]* ");

        builder.append(alert.severity).append(": *");
        if (alert.site != null) builder.append(alert.site).append(" / ");
        builder.append(alert.app).append("*\n");

        if (alert.host != null) builder.append("host: ").append(alert.host).append('\n');
        builder.append("_id: <").append(alert.docURL()).append('|').append(alert.id).append(">\n");
        if (alert.action != null) builder.append("action: ").append(alert.action).append('\n');

        builder.append("error_code: *").append(alert.errorCode).append("*\nmessage: ").append(alert.errorMessage).append('\n');

        return builder.toString();
    }

    String color(Severity severity, LocalDateTime date) {
        int week = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        int colorIndex = severity == Severity.WARN ? 0 : 1;
        return colors[colorIndex][(week - 1) % 2];
    }
}
