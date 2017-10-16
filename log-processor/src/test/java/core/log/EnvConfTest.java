package core.log;

import core.framework.test.EnvResourceValidator;
import core.framework.test.EnvWebValidator;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * @author neo
 */
class EnvConfTest {
    @Test
    void validateEnvResource() throws IOException {
        new EnvResourceValidator().validate();
    }

    @Test
    void validateEnvWeb() throws IOException {
        new EnvWebValidator().validate();
    }
}
