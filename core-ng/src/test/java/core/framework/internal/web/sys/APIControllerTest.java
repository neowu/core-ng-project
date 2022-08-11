package core.framework.internal.web.sys;

import core.framework.internal.web.api.APIDefinitionResponse;
import core.framework.internal.web.api.MessageAPIDefinitionResponse;
import core.framework.internal.web.service.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class APIControllerTest {
    private APIController controller;

    @BeforeEach
    void createAPIController() {
        controller = new APIController();
    }

    @Test
    void defaultBeanClasses() {
        assertThat(controller.beanClasses).contains(ErrorResponse.class);   // should publish default error response
    }

    @Test
    void serviceDefinition() {
        APIDefinitionResponse response = controller.serviceDefinition();

        assertThat(response).isNotNull();
        assertThat(controller.serviceInterfaces).isNull();
        assertThat(controller.beanClasses).isNull();
    }

    @Test
    void messageDefinition() {
        MessageAPIDefinitionResponse response = controller.messageDefinition();
        assertThat(response).isNotNull();
        assertThat(controller.topics).isNull();
    }
}
