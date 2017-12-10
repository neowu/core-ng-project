package core.framework.impl.web.api;

import core.framework.impl.web.service.TestWebService;
import core.framework.json.JSON;
import core.framework.util.ClasspathResources;
import core.framework.util.Types;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class OpenAPIManagerTest {
    private OpenAPIManager manager;

    @BeforeEach
    void createOpenAPIManager() {
        manager = new OpenAPIManager("local");
        manager.serviceInterfaces.add(TestWebService.class);
    }

    @Test
    void build() {
        Map<String, Object> document = manager.build();

        Map<String, Object> expected = JSON.fromJSON(Types.map(String.class, Object.class), ClasspathResources.text("openapi-test/document.json"));
        assertEquals(expected, document);
    }
}
