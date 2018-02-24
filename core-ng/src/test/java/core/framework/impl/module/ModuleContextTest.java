package core.framework.impl.module;

import core.framework.http.HTTPMethod;
import core.framework.impl.inject.BeanFactory;
import core.framework.impl.web.HTTPServerHealthCheckHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author neo
 */
class ModuleContextTest {
    private ModuleContext moduleContext;

    @BeforeEach
    void createModuleContext() {
        moduleContext = new ModuleContext(new BeanFactory(), null);
    }

    @Test
    void route() {
        Error error = assertThrows(Error.class, () -> moduleContext.route(HTTPMethod.GET, HTTPServerHealthCheckHandler.PATH, null, false));
        assertThat(error.getMessage()).contains("health-check path is reserved by framework");
    }
}
