package app.monitor.job;

import core.framework.api.http.HTTPStatus;
import core.framework.http.HTTPClient;
import core.framework.http.HTTPMethod;
import core.framework.http.HTTPRequest;
import core.framework.http.HTTPResponse;
import core.framework.internal.json.JSONReader;
import core.framework.util.Strings;

import java.io.IOException;

/**
 * @author neo
 */
public class ElasticSearchClient {
    private final JSONReader<ElasticSearchNodeStats> reader = new JSONReader<>(ElasticSearchNodeStats.class);
    private final HTTPClient httpClient = HTTPClient.builder().build();
    private final String host;
    private final String apiKey;

    public ElasticSearchClient(String host, String apiKey) {
        this.host = parseHostString(host);
        this.apiKey = apiKey;
    }

    private String parseHostString(String host) {
        String text = host;
        String scheme = "http";
        int port = 9200;
        final int schemeIdx = text.indexOf("://");
        if (schemeIdx > 0) {
            scheme = text.substring(0, schemeIdx);
            text = text.substring(schemeIdx + 3);
        }
        final int portIdx = text.lastIndexOf(':');
        if (portIdx > 0) {
            try {
                port = Integer.parseInt(text.substring(portIdx + 1));
            } catch (final NumberFormatException ex) {
                throw new Error("invalid host, host= " + host, ex);
            }
            text = text.substring(0, portIdx);
        }
        return Strings.format("{}://{}:{}", scheme, text, port);
    }

    // refer to https://www.elastic.co/guide/en/elasticsearch/reference/current/cluster-nodes-stats.html
    public ElasticSearchNodeStats stats() throws IOException {
        var request = new HTTPRequest(HTTPMethod.GET, host + "/_nodes/stats");
        if (apiKey != null) {
            request.headers.put("Authorization", "ApiKey " + apiKey);
        }
        request.params.put("metric", "indices,jvm,fs");
        request.params.put("filter_path", "nodes.*.name,nodes.*.indices.docs,nodes.*.jvm.mem,nodes.*.jvm.gc,nodes.*.fs.total");
        HTTPResponse response = httpClient.execute(request);
        if (response.statusCode != HTTPStatus.OK.code)
            throw new Error(Strings.format("failed to call elasticsearch node stats api, uri={}, status={}", request.requestURI(), response.statusCode));
        return reader.fromJSON(response.body);
    }
}
