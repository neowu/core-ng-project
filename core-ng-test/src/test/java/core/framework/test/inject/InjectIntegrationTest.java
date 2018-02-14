package core.framework.test.inject;

import core.framework.inject.Inject;
import core.framework.test.IntegrationTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class InjectIntegrationTest extends IntegrationTest {
    @Inject
    TestBean bean;

    @Test
    void bindInstance() {
        assertThat(bean.property).isEqualTo("value");   // from test.properties
        assertThat(bean.httpClient).isNotNull();
    }
}
