package core.framework.internal.db.cloud;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

/**
 * @author neo
 */
class GCloudAuthProviderTest {
    private GCloudAuthProvider provider;

    @BeforeEach
    void createCloudAuthProvider() {
        provider = spy(new GCloudAuthProvider());
        doReturn("lab-customer-service@lab.iam.gserviceaccount.com").when(provider).metadata("email");
        String tokenJSON = """
            {"access_token":"ya29.c.b0AXv0zTNPzp_Hl__8DJzw1KHKq_B9vhP7eHbRz18ar38gl_hbPt5YugF_2P7WOaqXPIM9JkllokdupT3vs7-rCssMoK0x2eguM8KG_wuOcDCJkUHNcDxriip6hq2Ww8FvE_XIlHivxMO607P1wIHIyv0kOV0iorcJ-G-1oEO4Pa5Rq9x1lQNXY2O3CoEjA4d8tFB0LPfkvfOOQpqpdwbww5LFiAFWy6OJzZphxxXisXaS_AIurTNduA0BQJViwprb8awSrUDbB3owais8wadFrZ-GV09aMqHvaORsydFb12T5TRhpzdqFSRr__H734VBH_if3XzbW5o2oqPUP9Ye3ynE9WYZqhBDHV7y0nqeNm5c2qD7KmRizO2ZdMzPxRVmQeafyOJEwb67H3vX3Cc6Wmz7Orujcas8R9UNNXDEPVy0GJdIlIXXFZJvwq0h95g.........................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................","expires_in":3561,"token_type":"Bearer"}""";

        doReturn(tokenJSON).when(provider).metadata("token");
    }

    @Test
    void user() {
        assertThat(provider.user())
            .isEqualTo("lab-customer-service")
            .isEqualTo(provider.user);
    }

    @Test
    void accessToken() {
        assertThat(provider.accessToken())
            .startsWith("ya29.c.b0AXv0zT").endsWith("....................")
            .isEqualTo(provider.accessToken);

        assertThat(Duration.ofMillis(provider.expirationTime - System.currentTimeMillis()))
            .isGreaterThan(Duration.ZERO)
            .isLessThan(Duration.ofHours(1));    // expires_in is 3561s

        // fetch again to use cache
        assertThat(provider.accessToken())
            .isEqualTo(provider.accessToken);
    }

    @Test
    void parseExpirationTimeInSec() {
        String tokenJSON = """
            {"access_token":"ya29.c.b0AXv0zTNPzp_Hl__8DJzw1KHKq_B9vhP7eHbRz18ar38gl_hbPt5YugF_2P7WOaqXPIM9JkllokdupT3vs7-rCssMoK0x2eguM8KG_wuOcDCJkUHNcDxriip6hq2Ww8FvE_XIlHivxMO607P1wIHIyv0kOV0iorcJ-G-1oEO4Pa5Rq9x1lQNXY2O3CoEjA4d8tFB0LPfkvfOOQpqpdwbww5LFiAFWy6OJzZphxxXisXaS_AIurTNduA0BQJViwprb8awSrUDbB3owais8wadFrZ-GV09aMqHvaORsydFb12T5TRhpzdqFSRr__H734VBH_if3XzbW5o2oqPUP9Ye3ynE9WYZqhBDHV7y0nqeNm5c2qD7KmRizO2ZdMzPxRVmQeafyOJEwb67H3vX3Cc6Wmz7Orujcas8R9UNNXDEPVy0GJdIlIXXFZJvwq0h95g.........................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................","expires_in":3561,"token_type":"Bearer"}""";

        assertThat(provider.parseExpirationTimeInSec(tokenJSON))
            .isEqualTo(3561);
    }
}
