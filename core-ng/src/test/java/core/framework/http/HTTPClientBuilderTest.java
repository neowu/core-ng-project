package core.framework.http;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class HTTPClientBuilderTest {
    private HTTPClientBuilder builder;

    @BeforeEach
    void createHTTPClientBuilder() {
        builder = HTTPClient.builder();
    }

    @Test
    void callTimeout() {
        builder.connectTimeout(Duration.ofSeconds(1));
        builder.timeout(Duration.ofSeconds(2));
        assertThat(builder.callTimeout()).isEqualTo(Duration.ofMillis(1000 + 2000 + 2000));

        builder.maxRetries(1);
        assertThat(builder.callTimeout()).isEqualTo(Duration.ofMillis(1000 + 2000 + 2000));

        builder.maxRetries(2);
        assertThat(builder.callTimeout()).isEqualTo(Duration.ofMillis(1000 + 2000 * 2 + 600 + 2000));

        builder.maxRetries(3);
        assertThat(builder.callTimeout()).isEqualTo(Duration.ofMillis(1000 + 2000 * 3 + 600 + 1200 + 2000));
    }

    @Test
    void trust() {
        String cert = """
                -----BEGIN CERTIFICATE-----
                MIIEoDCCAogCCQC5Kos+icdPjzANBgkqhkiG9w0BAQsFADASMRAwDgYDVQQDDAd1
                bmtub3duMB4XDTIwMDQwMTE5NTA0NloXDTMwMDMzMDE5NTA0NlowEjEQMA4GA1UE
                AwwHdW5rbm93bjCCAiIwDQYJKoZIhvcNAQEBBQADggIPADCCAgoCggIBAOs1QKoL
                rmz3QcOLlhZl3Wpv2lDaxTt3ZuZalL9ISx9O0MTz5jvXW3uJWbn4ceQTxGQkJ9aL
                QkIyR73ZK4P0RvGZY9zLzPsM84FS4WXS0UEDYw39TTMAYy/XPs/DmThPZAR1fttG
                LHtuTyiWZ4IwqXcVKzwPVROiSMVTcfMn/Tf0pSpppnE6HqcicPmoTFK6MwAZ+AAw
                ewE3c7YpnrPlG8U3ll7WXVnenAEtVO4yu3jpPUEavb9KfuEbyPaXO3hKntGsER7O
                Y2hQ/2zEozaHLVsXiJ5Chsdgrow7e8fs2VZU5WbuBWE6i3sijjPPqwTYtGP8ABPf
                vUI6NUhMUXa/Dz00A0W04Yx9DSUd/dekTrm126ewp6zKH3v6RiluvsOC9gTjdjc7
                qmuoO32M1NWVYx0RhWKL5HmZASWA7nGGmHS7PSqu3csz8I2vNA5SBFh5+WSwtpoh
                3ta6MVkneETNWH9K5UqVZ9DDNtFGghGgvqcEyoQurPhKrIcq9oj1clR8+B1X7clY
                bwSHN1iRj0ThlFbFAO5j+5apT+FCbQ3qW1OWrvMI01fqjdzRBlUhVdPfYK6awdOO
                kfzmozOKgNHCv/83c7PUKpMCQtpJFgljNckctM1TGdn9TAnYMXWrAfxcakUblJ0q
                HbdDR8Qvubrq8LPE7D9VX5/vIOvdi+Re3f3XAgMBAAEwDQYJKoZIhvcNAQELBQAD
                ggIBAD1pPUBPDi/QTcFfFwGGBwkwygCaAjaKwPSVEXy1YiMiU8kVKfExzZG8gjGy
                4sYCsHAmkdHe5DLr0BCPmkTq71zSarsEPeLdlA0D+pIQwX4GWDCZyPzbh7IRPWC8
                A2DIXvUObsYR/J/umR7W4voWKky88UdHxDnYrzcRLhnKJUfpsfUiF8jOLgwMxIDE
                3k/+5DqCoVtI2zjmGCahPJ2Rsnz8WKosBRvHCzVWlcytOWFzAyyP3f0LfXGGX04l
                NvXcqTxcHm2F+k56y2pHGir0IyR0zMMi1m2Hjr1kLCP0aSlrrGilo4bQrD1Qzz9o
                oxcCbhfVBlVe9BWtVPRAen0/GVKxM2mpO2zZO6ckQqdQYPRoVcmvi9v8Hv4Frwlj
                8IeXq0ynzklaepu7jLEAuAOZXGQjZm0bTFSNE8HImwTh+Wc0useMGrNo44t03/+x
                eEN07j16rE8iMyG1TKSr0GzvpZjwgAthPSmCyKvJi1U8fT26ZStQXIFErKZ4Izk5
                P9hEBzjd6+qe+9CxSWO0fM1hHCu7g2ADYjDUuV5VoUVtkZWpiPnM+CY/rmdfwElK
                Rc951Uzxq+tSNopgkDbeMLc78oCneiIkG5kRtGSg0cq9F6PvAsSq/wHH2Pt/CeLG
                T6iN3OqSp9rSOWhhEjIdDLmv47tBBg006TxZdgFHItcYxBj2
                -----END CERTIFICATE-----
                """;
        builder.trust(cert).trust(cert).build();
    }

    @Test
    void trustAll() {
        builder.trustAll().build();
    }
}
