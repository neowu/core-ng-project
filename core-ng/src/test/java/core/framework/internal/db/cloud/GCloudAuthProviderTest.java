package core.framework.internal.db.cloud;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class GCloudAuthProviderTest {
    private GCloudAuthProvider provider;

    @BeforeEach
    void createCloudAuthProvider() {
        provider = new GCloudAuthProvider();
    }

    @Test
    void parseUser() {
        assertThat(provider.parseUser("lab-customer-service@lab.iam.gserviceaccount.com")).isEqualTo("lab-customer-service");
    }

    @Test
    void parsePassword() {
        String tokenJSON = """
            {"access_token":"ya29.c.b0AXv0zTNPzp_Hl__8DJzw1KHKq_B9vhP7eHbRz18ar38gl_hbPt5YugF_2P7WOaqXPIM9JkllokdupT3vs7-rCssMoK0x2eguM8KG_wuOcDCJkUHNcDxriip6hq2Ww8FvE_XIlHivxMO607P1wIHIyv0kOV0iorcJ-G-1oEO4Pa5Rq9x1lQNXY2O3CoEjA4d8tFB0LPfkvfOOQpqpdwbww5LFiAFWy6OJzZphxxXisXaS_AIurTNduA0BQJViwprb8awSrUDbB3owais8wadFrZ-GV09aMqHvaORsydFb12T5TRhpzdqFSRr__H734VBH_if3XzbW5o2oqPUP9Ye3ynE9WYZqhBDHV7y0nqeNm5c2qD7KmRizO2ZdMzPxRVmQeafyOJEwb67H3vX3Cc6Wmz7Orujcas8R9UNNXDEPVy0GJdIlIXXFZJvwq0h95g.........................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................","expires_in":3561,"token_type":"Bearer"}""";

        assertThat(provider.parsePassword(tokenJSON))
            .startsWith("ya29.c.b0AXv0zT").endsWith("....................");
    }
}
