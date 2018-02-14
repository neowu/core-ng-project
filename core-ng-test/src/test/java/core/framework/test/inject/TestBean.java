package core.framework.test.inject;

import core.framework.http.HTTPClient;
import core.framework.inject.Inject;

/**
 * @author neo
 */
public class TestBean {
    final String property;

    @Inject
    HTTPClient httpClient;

    public TestBean(String property) {
        this.property = property;
    }
}
