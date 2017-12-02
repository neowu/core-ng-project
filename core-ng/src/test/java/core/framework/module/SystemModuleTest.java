package core.framework.module;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class SystemModuleTest {
    private SystemModule systemModule;

    @BeforeEach
    void createSystemModule() {
        systemModule = new SystemModule(null);
    }

    @Test
    void httpPort() {
        System.setProperty("sys.http.httpPort", "8081");

        assertEquals((Integer) 8081, systemModule.httpPort("sys.http.httpPort"));
    }
}
