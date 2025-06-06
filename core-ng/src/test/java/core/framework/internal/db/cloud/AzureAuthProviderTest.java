package core.framework.internal.db.cloud;

import core.framework.http.HTTPRequest;
import core.framework.internal.db.Dialect;
import core.framework.util.Strings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

/**
 * @author miller
 */
class AzureAuthProviderTest {
    private AzureAuthProvider provider;

    @BeforeEach
    void createAzureAuthProvider() {
        provider = spy(new AzureAuthProvider("some-service"));
    }

    @Test
    void user() {
        assertThat(provider.user(Dialect.MYSQL))
            .isEqualTo("some-service");
    }

    @Test
    void accessToken() {
        String tokenJSON = """
            {"token_type":"Bearer","expires_in":86399,"ext_expires_in":86399,"access_token":"eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6IjVCM25SeHRRN2ppOGVORGMzRnkwNUtmOTdaRSIsImtpZCI6IjVCM25SeHRRN2ppOGVORGMzRnkwNU__________.eyJhdWQiOiJodHRwczovL29zc3JkYm1zLWFhZC5kYXRhYmFzZS53aW5kb3dzLm5ldCIsImlzcyI6Imh0dHBzOi8vc3RzLndpbmRvd3MubmV0L2I4YjU4YzA5LWM1ZjMtNDA0ZC04OTYwLTlkMmFlMjYwNTU3My8iLCJpYXQiOjE3MDU5MjA4NjMsIm5iZiI6MTcwNTkyMDg2MywiZXhwIjoxNzA2MDA3NTYzLCJhaW8iOiJFMlZnWUpDeWZTMHR2a0YvUjgwYWVVa1I1YzlTQUE9PSIsImFwcGlkIjoiMzRlMGM0NDMtNTg0Yy00MzhlLWIwN2QtMzBmYTk0YzA1ZWVlIiwiYXBwaWRhY3IiOiIyIiwiaWRwIjoiaHR0cHM6Ly9zdHMud2luZG93cy5uZXQvYjhiNThjMDktYzVmMy00MDRkLTg5NjAtOWQyYWUyNjA1NTczLyIsIm9pZCI6IjBiOGEzMjBhLTI0NWYtNGYyZS1hMTJmLWQzZjkxYTlmNjg5NiIsInJoIjoiMC5BU2dBQ1l5MXVQUEZUVUNKWUowcTRtQlZjMURZUEJMZjJiMUFsTlhKOEh0X29nTW9BQUEuIiwic3ViIjoiMGI4YTMyMGEtMjQ1Zi00ZjJlLWExMmYtZDNmOTFhOWY2ODk2IiwidGlkIjoiYjhiNThjMDktYzVmMy00MDRkLTg5NjAtOWQyYWUyNjA1NTczIiwidXRpIjoiQWZIYlpvMWQ2VW1pUDdTSkZ4ODVBQSIsInZlci__________.cacH4-4tHmv9p1MqBxpu7mhxCPwotLGyq8ioN60Ki0gPCFyAaHSYRWogyLrr2IZelOSsZfdjj0AOlDqSRwtu740uML3_TPuQjpWQtOvoaY9WPFrXriPVayhf-qQNP8IyaKnzkrDa6OcgCRPvZMh0RAxHvaN1r5v1X2oZmsgOMBH-rybrOKMJygEMLSJveyybFDDd0C3AC7ACS1JePYVYfIHsUIpnZW4WB6OhoqjsY6dizlYqBbvL8t68h1dFhLfZtu3I1BgeHeOeqVo0XhJdXEMWbWSIJS94QLjn0-we1_bEmLUTaAvOLValrqKm9FPaOU7eq_R-...-.........."}""";
        doReturn(tokenJSON).when(provider).exchangeToken();

        assertThat(provider.accessToken())
            .startsWith("eyJ0eXAiOiJKV1QiLCJhbGci").endsWith("...-..........")
            .isEqualTo(provider.accessToken);

        assertThat(Duration.ofMillis(provider.expirationTime - System.currentTimeMillis()))
            .isGreaterThan(Duration.ZERO)
            .isLessThan(Duration.ofDays(1));    // expires_in is 86399s

        // fetch again to use cache
        assertThat(provider.accessToken())
            .isEqualTo(provider.accessToken);
    }

    @Test
    void parseExpirationTimeInSec() {
        String tokenJSON = """
            {"token_type":"Bearer","expires_in":86399,"ext_expires_in":86399,"access_token":"eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6IjVCM25SeHRRN2ppOGVORGMzRnkwNUtmOTdaRSIsImtpZCI6IjVCM25SeHRRN2ppOGVORGMzRnkwNU__________.eyJhdWQiOiJodHRwczovL29zc3JkYm1zLWFhZC5kYXRhYmFzZS53aW5kb3dzLm5ldCIsImlzcyI6Imh0dHBzOi8vc3RzLndpbmRvd3MubmV0L2I4YjU4YzA5LWM1ZjMtNDA0ZC04OTYwLTlkMmFlMjYwNTU3My8iLCJpYXQiOjE3MDU5MjA4NjMsIm5iZiI6MTcwNTkyMDg2MywiZXhwIjoxNzA2MDA3NTYzLCJhaW8iOiJFMlZnWUpDeWZTMHR2a0YvUjgwYWVVa1I1YzlTQUE9PSIsImFwcGlkIjoiMzRlMGM0NDMtNTg0Yy00MzhlLWIwN2QtMzBmYTk0YzA1ZWVlIiwiYXBwaWRhY3IiOiIyIiwiaWRwIjoiaHR0cHM6Ly9zdHMud2luZG93cy5uZXQvYjhiNThjMDktYzVmMy00MDRkLTg5NjAtOWQyYWUyNjA1NTczLyIsIm9pZCI6IjBiOGEzMjBhLTI0NWYtNGYyZS1hMTJmLWQzZjkxYTlmNjg5NiIsInJoIjoiMC5BU2dBQ1l5MXVQUEZUVUNKWUowcTRtQlZjMURZUEJMZjJiMUFsTlhKOEh0X29nTW9BQUEuIiwic3ViIjoiMGI4YTMyMGEtMjQ1Zi00ZjJlLWExMmYtZDNmOTFhOWY2ODk2IiwidGlkIjoiYjhiNThjMDktYzVmMy00MDRkLTg5NjAtOWQyYWUyNjA1NTczIiwidXRpIjoiQWZIYlpvMWQ2VW1pUDdTSkZ4ODVBQSIsInZlci__________.cacH4-4tHmv9p1MqBxpu7mhxCPwotLGyq8ioN60Ki0gPCFyAaHSYRWogyLrr2IZelOSsZfdjj0AOlDqSRwtu740uML3_TPuQjpWQtOvoaY9WPFrXriPVayhf-qQNP8IyaKnzkrDa6OcgCRPvZMh0RAxHvaN1r5v1X2oZmsgOMBH-rybrOKMJygEMLSJveyybFDDd0C3AC7ACS1JePYVYfIHsUIpnZW4WB6OhoqjsY6dizlYqBbvL8t68h1dFhLfZtu3I1BgeHeOeqVo0XhJdXEMWbWSIJS94QLjn0-we1_bEmLUTaAvOLValrqKm9FPaOU7eq_R-...-.........."}""";

        assertThat(provider.parseExpirationTimeInSec(tokenJSON))
            .isEqualTo(86399);
    }

    @Test
    void exchangeRequest() {
        String clientId = "aaaaaaaa-xxxx-aaaa-aaaa-aaaaaaaaaaaa";
        String tenantId = "bbbbbbbb-xxxx-bbbb-bbbb-bbbbbbbbbbbb";
        String tokenFilePath = "azure-auth-provider-test/azureFederatedToken";
        String token = "7f85e0f9-60c9-4c75-b399-1b01f1a611c5________.45623123-c485-4612-a458-2a1caf45c4e9________.27b20a02-718f-45cc-90cf-e294248aa433________";
        doReturn("https://login.microsoftonline.com/").when(provider).env("AZURE_AUTHORITY_HOST");
        doReturn(clientId).when(provider).env("AZURE_CLIENT_ID");
        doReturn(tenantId).when(provider).env("AZURE_TENANT_ID");
        doReturn(tokenFilePath).when(provider).env("AZURE_FEDERATED_TOKEN_FILE");
        doReturn(token).when(provider).azureFederatedToken(tokenFilePath);

        HTTPRequest request = provider.exchangeRequest();
        assertThat(request.uri).isEqualTo(Strings.format("https://login.microsoftonline.com/bbbbbbbb-xxxx-bbbb-bbbb-bbbbbbbbbbbb/oauth2/v2.0/token"));
        assertThat(request.form.get("client_assertion")).isEqualTo(token);
        assertThat(request.form.get("client_assertion_type")).isEqualTo("urn:ietf:params:oauth:client-assertion-type:jwt-bearer");
        assertThat(request.form.get("client_id")).isEqualTo(clientId);
        assertThat(request.form.get("grant_type")).isEqualTo("client_credentials");
        assertThat(request.form.get("scope")).isEqualTo("https://ossrdbms-aad.database.windows.net/.default");
    }
}
