package app.monitor.job;

import core.framework.api.http.HTTPStatus;
import core.framework.http.ContentType;
import core.framework.http.HTTPClient;
import core.framework.http.HTTPMethod;
import core.framework.http.HTTPRequest;
import core.framework.http.HTTPResponse;
import core.framework.internal.json.JSONReader;
import core.framework.util.Files;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author neo
 */
public class KubeClient {
    private final JSONReader<KubePodList> reader = new JSONReader<>(KubePodList.class);
    private HTTPClient httpClient;
    private String token;

    // only support Pod ServiceAccount auth within cluster
    public void initialize() {
        httpClient = HTTPClient.builder()
            .trust(Files.text(Path.of("/var/run/secrets/kubernetes.io/serviceaccount/ca.crt")))
            .maxRetries(3)
            .build();
        // token will not be refreshed after pod created, if the token is changed somehow, it has to recreate pod
        token = Files.text(Path.of("/var/run/secrets/kubernetes.io/serviceaccount/token"));
    }

    public KubePodList listPods(String namespace) throws IOException {
        var request = new HTTPRequest(HTTPMethod.GET, "https://kubernetes.default.svc/api/v1/namespaces/" + namespace + "/pods");
        request.bearerAuth(token);
        request.accept(ContentType.APPLICATION_JSON);
        HTTPResponse response = httpClient.execute(request);
        if (response.statusCode != HTTPStatus.OK.code) {
            throw new Error("failed to call kube api server, statusCode=" + response.statusCode + ", message=" + response.text());
        }
        // not using validation to reduce overhead
        return reader.fromJSON(response.body);
    }
}
