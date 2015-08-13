package core.log;

import core.framework.test.EnvConfValidator;
import org.junit.Test;

/**
 * @author neo
 */
public class EnvConfTest {
    @Test
    public void validateEnvConf() {
        new EnvConfValidator().validate();
    }
}
