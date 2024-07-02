package core.framework.internal.db.cloud;

import core.framework.db.CloudAuthProvider;
import core.framework.http.HTTPClient;
import core.framework.http.HTTPMethod;
import core.framework.http.HTTPRequest;
import core.framework.http.HTTPResponse;
import core.framework.util.Files;
import core.framework.util.Strings;

import java.nio.file.Path;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author miller
 * This provider is designed for use in the AKS environment.
 */
public class AzureAuthProvider implements CloudAuthProvider {
    private static final String AZURE_AUTHORITY_HOST = "AZURE_AUTHORITY_HOST";
    private static final String AZURE_CLIENT_ID = "AZURE_CLIENT_ID";
    private static final String AZURE_TENANT_ID = "AZURE_TENANT_ID";
    private static final String AZURE_FEDERATED_TOKEN_FILE = "AZURE_FEDERATED_TOKEN_FILE";
    //refers to com.azure.identity.extensions.implementation.token.AccessTokenResolverOptions
    private static final Map<String, String> OSS_RDBMS_SCOPE_MAP = Map.of(
        "https://login.microsoftonline.com/", "https://ossrdbms-aad.database.windows.net/.default",
        "https://login.chinacloudapi.cn/", "https://ossrdbms-aad.database.chinacloudapi.cn/.default",
        "https://login.microsoftonline.de/", "https://ossrdbms-aad.database.cloudapi.de/.default",
        "https://login.microsoftonline.us/", "https://ossrdbms-aad.database.usgovcloudapi.net/.default");
    private final HTTPClient httpClient = HTTPClient.builder()
        .connectTimeout(Duration.ofMillis(500))
        .timeout(Duration.ofSeconds(1))
        .maxRetries(3)
        .retryWaitTime(Duration.ofMillis(50))
        .build();
    private final String user;
    String accessToken;
    long expirationTime;

    public AzureAuthProvider(String user) {
        this.user = user;
    }

    @Override
    public String user() {
        return user;
    }

    @Override
    public String accessToken() {
        if (accessToken != null && System.currentTimeMillis() < expirationTime) {
            return accessToken;
        }

        long now = System.currentTimeMillis();  // record current time, to make sure expire time is always earlier than metadata side
        String tokenJSON = exchangeToken();
        accessToken = parseAccessToken(tokenJSON);
        // token will be refreshed 300s before expiration, and connected connection will not be invalidated
        // set expirationTime after access token, to handle multi thread conditions
        expirationTime = now + (parseExpirationTimeInSec(tokenJSON) - 300) * 1_000L;
        return accessToken;
    }

    String exchangeToken() {
        HTTPRequest httpRequest = exchangeRequest();
        HTTPResponse response = httpClient.execute(httpRequest);
        if (response.statusCode != 200) {
            throw new Error("failed to exchange Azure accessToken, status=" + response.statusCode + ", response=" + response.text());
        }
        return response.text();
    }

    private HTTPRequest exchangeRequest() {
        String azureAuthorityHost = System.getenv(AZURE_AUTHORITY_HOST);
        String identityClientId = System.getenv(AZURE_CLIENT_ID);
        String identityTenantId = System.getenv(AZURE_TENANT_ID);
        String azureAuthorityURL = Strings.format("{}{}/oauth2/v2.0/token", azureAuthorityHost, identityTenantId);

        String scope = OSS_RDBMS_SCOPE_MAP.get(azureAuthorityHost);
        String federatedToken = Files.text(Path.of(System.getenv(AZURE_FEDERATED_TOKEN_FILE)));
        Map<String, String> form = new LinkedHashMap<>();
        form.put("client_assertion", federatedToken);
        form.put("client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer");
        form.put("client_id", identityClientId);
        form.put("grant_type", "client_credentials");
        form.put("scope", scope);

        var request = new HTTPRequest(HTTPMethod.POST, azureAuthorityURL);
        request.form(form);
        return request;
    }

    private String parseAccessToken(String tokenJSON) {
        int startIndex = tokenJSON.indexOf("\"access_token\":\"") + 16;
        int endIndex = tokenJSON.indexOf('"', startIndex);
        return tokenJSON.substring(startIndex, endIndex);
    }

    int parseExpirationTimeInSec(String tokenJSON) {
        int startIndex = tokenJSON.indexOf("\"expires_in\":") + 13;
        int endIndex = tokenJSON.indexOf(',', startIndex);
        return Integer.parseInt(tokenJSON.substring(startIndex, endIndex));
    }
}
