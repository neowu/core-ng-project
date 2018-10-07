package core.framework.crypto;

import core.framework.util.ClasspathResources;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class PEMTest {
    @Test
    void convert() {
        String cert = ClasspathResources.text("crypto-test/signature-cert.pem");

        byte[] content = PEM.fromPEM(cert);
        assertThat(content).isNotEmpty();

        String pem = PEM.toPEM("CERTIFICATE", content);
        assertThat(pem).isEqualToIgnoringNewLines(cert);
    }
}
