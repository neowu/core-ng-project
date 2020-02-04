package app.monitor.slack;

import core.framework.http.ContentType;
import core.framework.http.HTTPClient;
import core.framework.http.HTTPHeaders;
import core.framework.http.HTTPMethod;
import core.framework.http.HTTPRequest;
import core.framework.http.HTTPResponse;
import core.framework.json.Bean;

import java.util.List;

/**
 * @author ericchung
 */
public class SlackClient {
    private static final String SLACK_API_URL = "https://slack.com/api/chat.postMessage";
    private final String token;
    private final HTTPClient httpClient;

    public SlackClient(HTTPClient httpClient, String token) {
        this.httpClient = httpClient;
        this.token = token;
    }

    public void send(String channel, String message, String color) {
        SlackMessageAPIRequest request = request(channel, message, color);

        var httpRequest = new HTTPRequest(HTTPMethod.POST, SLACK_API_URL);
        httpRequest.headers.put(HTTPHeaders.AUTHORIZATION, "Bearer " + token);
        httpRequest.body(Bean.toJSON(request), ContentType.APPLICATION_JSON);
        HTTPResponse httpResponse = httpClient.execute(httpRequest);

        SlackMessageAPIResponse response = Bean.fromJSON(SlackMessageAPIResponse.class, httpResponse.text());

        if (!Boolean.TRUE.equals(response.ok)) {
            throw new Error("failed to send message to slack");
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
}
