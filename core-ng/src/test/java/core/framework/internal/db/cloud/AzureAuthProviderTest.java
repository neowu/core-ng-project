package core.framework.internal.db.cloud;

import core.framework.http.HTTPRequest;
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
        assertThat(provider.user())
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
        String clientId = "ac6ca327-xxxx-4406-b554-f7a128a15ccd";
        String tenantId = "b8b58c09-xxxx-404d-8960-9d2ae2605573";
        String tokenFilePath = "azure-auth-provider-test/azureFederatedToken";
        String token = "eyJhbGciOiJSUzI1NiIsImtpZCI6IkNuSkVUenhHS09uUHd0SF9feEM4aExoMFVJb0FoQzI4emtlRFdhZ2________.eyJhdWQiOlsiYXBpOi8vQXp1cmVBRFRva2VuRXhjaGFuZ2UiXSwiZXhwIjoxNzE5OTc2NTIwLCJpYXQiOjE3MTk5NzI5MjAsImlzcyI6Imh0dHBzOi8vZWFzdHVzLm9pYy5wcm9kLWFrcy5henVyZS5jb20vYjhiNThjMDktYzVmMy00MDRkLTg5NjAtOWQyYWUyNjA1NTczLzE4N2YwNmFhLTlmMzItNGMwZi1iMzg3LThkY2ViOWVhMDBlMi8iLCJrdWJlcm5ldGVzLmlvIjp7Im5hbWVzcGFjZSI6ImRldi1wbGF0Zm9ybS1zZXJ2aWNlcyIsInBvZCI6eyJuYW1lIjoiYXBpLWRvYy1zZXJ2aWNlLTU1ZGNmZDc5OWQtOGw1cXIiLCJ1aWQiOiI0NGI4MWM2ZC0wM2JlLTQ5YzQtOWJjYi00ZjU3ZmE2NGE5NjkifSwic2VydmljZWFjY291bnQiOnsibmFtZSI6ImFwaS1kb2Mtc2VydmljZSIsInVpZCI6Ijk2YTEyMTY1LTRhZmItNDU5Yi1iZTFlLWZhYjMwOTEzNDhhYSJ9fSwibmJmIjoxNzE5OTcyOTIwLCJzdWIiOiJzeXN0ZW06c2VydmljZWFjY291bnQ6ZGV2LXBsYXRmb3JtLXNlcnZpY2VzOmFwaS1kb2Mtc2Vy________.oS8v_7Z8UdclIC0AkhHQzQ2HzB39T1KBoSx6Gt0BYQsvsAugjDrKbO6qGN3eSs20BX3XnmW-2Q-Z4PREcf9q9KJEzBwGeNyj8Ti11Y9PHQjFKcu0aF0PEh6P34ypmm_s-dvtDOt2QVy7dHipuBj6GNYXs22ZQGZEfeWzsFwnFZORj8OGLKw-MY93chwBtAMEd8utD12T8ZbOq_YP7GIgFBD46ZbjXgcTggWdrTMJDzf92B2iI71XpFXnHG8VxOjpxd8fOo5J0iiq4BSh3SnVHyCKQg4Bfu8RB2ttMk1fvEXD6B4moy-gHjnlzwxIbVkgg0iJ6KWEkR8y-qwPbtntQ-SNwqcxGKtC8Re69nMRDmF8bWzzvnRPl5cRn4B8d4taswysCqS4Qt_Ywo0h-iznNaYQQr1_KykY6VpCtewUpCpjYKSOGeLUcno85aJo7YnNpAT53PCIweqsqcLXVL9r8TBcY2qHbcvCmJvemh-5bHNLFcuOAZ_bjsPDHPcuRLB1fq8MjSFdwMumPjeaGFBDb_EQ0UWCOxOnS956w39w5Jiv7wlbkacBeg3s5Jn4ic9ryBcPDQqjiVLuviSyRbHVhHxnMTi56qr-KF9-DqsFWI5yZkI-LwTb99TO1-qdVsml7DCvFp8ABb5_T2M29Z63G15gD10ZVmdJ1AB________";
        doReturn("https://login.microsoftonline.com/").when(provider).env("AZURE_AUTHORITY_HOST");
        doReturn(clientId).when(provider).env("AZURE_CLIENT_ID");
        doReturn(tenantId).when(provider).env("AZURE_TENANT_ID");
        doReturn(tokenFilePath).when(provider).env("AZURE_FEDERATED_TOKEN_FILE");
        doReturn(token).when(provider).azureFederatedToken(tokenFilePath);

        HTTPRequest request = provider.exchangeRequest();
        assertThat(request.uri).isEqualTo(Strings.format("https://login.microsoftonline.com/b8b58c09-xxxx-404d-8960-9d2ae2605573/oauth2/v2.0/token"));
        assertThat(request.form.get("client_assertion")).isEqualTo(token);
        assertThat(request.form.get("client_assertion_type")).isEqualTo("urn:ietf:params:oauth:client-assertion-type:jwt-bearer");
        assertThat(request.form.get("client_id")).isEqualTo(clientId);
        assertThat(request.form.get("grant_type")).isEqualTo("client_credentials");
        assertThat(request.form.get("scope")).isEqualTo("https://ossrdbms-aad.database.windows.net/.default");
    }
}
