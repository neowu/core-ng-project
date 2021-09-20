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

    public ElasticSearchNodeStats stats(String host) throws IOException {
        return stats(host, null);
    }

    // refer to https://www.elastic.co/guide/en/elasticsearch/reference/current/cluster-nodes-stats.html
    public ElasticSearchNodeStats stats(String host, String apiKey) throws IOException {
        var request = new HTTPRequest(HTTPMethod.GET, "http://" + host + ":9200/_nodes/stats");
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
