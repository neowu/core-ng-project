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
import java.time.Instant;

/**
 * @author neo
 */
public class KubeClient {
    private final JSONReader<KubePodList> reader = new JSONReader<>(KubePodList.class);

    Instant lastUpdateTime;
    String token;

    private HTTPClient httpClient;

    // only support Pod ServiceAccount auth within cluster
    public void initialize() {
        httpClient = HTTPClient.builder()
            .trust(Files.text(Path.of("/var/run/secrets/kubernetes.io/serviceaccount/ca.crt")))
            .maxRetries(3)
            .build();
    }

    public KubePodList listPods(String namespace) throws IOException {
        var request = new HTTPRequest(HTTPMethod.GET, "https://kubernetes.default.svc/api/v1/namespaces/" + namespace + "/pods");
        request.bearerAuth(token(Instant.now()));
        request.accept(ContentType.APPLICATION_JSON);
        HTTPResponse response = httpClient.execute(request);
        if (response.statusCode != HTTPStatus.OK.code) {
            throw new Error("failed to call kube api server, statusCode=" + response.statusCode + ", message=" + response.text());
        }
        // not using validation to reduce overhead
        return reader.fromJSON(response.body);
    }

    String token(Instant now) {
        // cache token 600s, kube token expires in 3607s, and refreshes every 48~49 minutes
        //   kube-api-access-8rm8j:
        //    Type:                    Projected (a volume that contains injected data from multiple sources)
        //    TokenExpirationSeconds:  3607
        //    ConfigMapName:           kube-root-ca.crt
        //    ConfigMapOptional:       <nil>
        //    DownwardAPI:             true
        if (lastUpdateTime == null || now.isAfter(lastUpdateTime.plusSeconds(600))) {
            lastUpdateTime = now;
            token = Files.text(Path.of("/var/run/secrets/kubernetes.io/serviceaccount/token"));
        }
        return token;
    }
}
