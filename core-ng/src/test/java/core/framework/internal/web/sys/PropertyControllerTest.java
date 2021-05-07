package core.framework.internal.web.sys;

import core.framework.http.ContentType;
import core.framework.internal.module.PropertyManager;
import core.framework.web.Request;
import core.framework.web.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
class PropertyControllerTest {
    private PropertyController controller;
    private PropertyManager propertyManager;

    @BeforeEach
    void createPropertyController() {
        propertyManager = new PropertyManager();
        controller = new PropertyController(propertyManager);
    }

    @Test
    void execute() {
        var request = mock(Request.class);
        when(request.clientIP()).thenReturn("127.0.0.1");

        Response response = controller.execute(request);
        assertThat(response.contentType()).get().isEqualTo(ContentType.TEXT_PLAIN);
    }

    @Test
    void properties() {
        propertyManager.properties.set("sys.jdbc.user", "user");
        propertyManager.properties.set("sys.jdbc.password", "password");

        assertThat(controller.properties())
                .contains("sys.jdbc.password=******")
                .contains("sys.jdbc.user=user");
    }
}
