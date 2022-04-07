package core.framework.internal.db.cloud;

import core.framework.http.HTTPClient;
import core.framework.http.HTTPMethod;
import core.framework.http.HTTPRequest;
import core.framework.http.HTTPResponse;

import java.time.Duration;
import java.util.Properties;

/**
 * @author neo
 */
public class GCloudAuthProvider {
    // for timeout settings, refer to gcloud sdk com.google.auth.oauth2.ComputeEngineCredentials#runningOnComputeEngine
    private final HTTPClient httpClient = HTTPClient.builder()
        .connectTimeout(Duration.ofMillis(500))
        .timeout(Duration.ofSeconds(1))
        .maxRetries(3)
        .retryWaitTime(Duration.ofMillis(50))
        .build();

    public void fetchCredential(Properties properties) {
        // properties are thread safe, it's ok to set user/password with multiple threads
        // and email won't change once pod created
        properties.setProperty("user", parseUser(metadata("email")));
        properties.setProperty("password", parsePassword(metadata("token")));
    }

    String parseUser(String email) {
        int index = email.indexOf('@');
        return email.substring(0, index);
    }

    String parsePassword(String tokenJSON) {
        // use fastest way to parse to reduce overhead
        // example value: {"access_token":"ya29.c.b0AXv0zTNPzp_Hl__8DJzw1KHKq_B9vhP7eHbRz18ar38gl_hbPt5YugF_2P7WOaqXPIM9JkllokdupT3vs7-rCssMoK0x2eguM8KG_wuOcDCJkUHNcDxriip6hq2Ww8FvE_XIlHivxMO607P1wIHIyv0kOV0iorcJ-G-1oEO4Pa5Rq9x1lQNXY2O3CoEjA4d8tFB0LPfkvfOOQpqpdwbww5LFiAFWy6OJzZphxxXisXaS_AIurTNduA0BQJViwprb8awSrUDbB3owais8wadFrZ-GV09aMqHvaORsydFb12T5TRhpzdqFSRr__H734VBH_if3XzbW5o2oqPUP9Ye3ynE9WYZqhBDHV7y0nqeNm5c2qD7KmRizO2ZdMzPxRVmQeafyOJEwb67H3vX3Cc6Wmz7Orujcas8R9UNNXDEPVy0GJdIlIXXFZJvwq0h95g.........................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................","expires_in":3561,"token_type":"Bearer"}
        int index = tokenJSON.indexOf('"', 17);
        return tokenJSON.substring(17, index);
    }

    private String metadata(String attribute) {
        // 169.254.169.254 is metadata.google.internal, use ip to save dns query
        var request = new HTTPRequest(HTTPMethod.GET, "http://169.254.169.254/computeMetadata/v1/instance/service-accounts/default/" + attribute);
        request.headers.put("Metadata-Flavor", "Google");
        HTTPResponse response = httpClient.execute(request);
        if (response.statusCode != 200) throw new Error("failed to fetch gcloud iam metadata, status=" + response.statusCode);
        return response.text();
    }
}
