package app;

import core.framework.test.EnvResourceValidator;
import core.framework.test.EnvWebValidator;
import org.junit.Test;

import java.io.IOException;

/**
 * @author neo
 */
public class EnvConfTest {
    @Test
    public void validateEnvResource() throws IOException {
        new EnvResourceValidator().validate();
    }

    @Test
    public void validateEnvWeb() throws IOException {
        new EnvWebValidator().validate();
    }
}
