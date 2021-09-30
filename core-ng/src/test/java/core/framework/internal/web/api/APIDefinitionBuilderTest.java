package core.framework.internal.web.api;

import core.framework.api.json.Property;
import core.framework.internal.web.service.TestWebService;
import core.framework.json.JSON;
import core.framework.util.ClasspathResources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class APIDefinitionBuilderTest {
    private APIDefinitionBuilder builder;

    @BeforeEach
    void createAPIDefinitionBuilder() {
        builder = new APIDefinitionBuilder(Set.of(TestWebService.class), Set.of(ErrorCode.class));
    }

    @Test
    void build() {
        APIDefinitionResponse response = builder.build();
        APIDefinitionResponse expectedResponse = JSON.fromJSON(APIDefinitionResponse.class, ClasspathResources.text("api-test/test-webservice.json"));
        assertThat(response).usingRecursiveComparison()
            .ignoringFields("version")
            .isEqualTo(expectedResponse);
    }

    public enum ErrorCode {
        @Property(name = "ERROR_CODE_1")
        ERROR_CODE_1,
        @Property(name = "ERROR_CODE_2")
        ERROR_CODE_2
    }
}
