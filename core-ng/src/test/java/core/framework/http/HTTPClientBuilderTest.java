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
        builder.retryWaitTime(Duration.ofMillis(500));
        assertThat(builder.callTimeout()).isEqualTo(Duration.ofMillis(1000 + 2000 + 2000));

        builder.maxRetries(1);
        assertThat(builder.callTimeout()).isEqualTo(Duration.ofMillis(1000 + 2000 + 2000));

        builder.maxRetries(2);
        assertThat(builder.callTimeout()).isEqualTo(Duration.ofMillis(1000 + 2000 * 2 + 500 + 2000));

        builder.maxRetries(3);
        assertThat(builder.callTimeout()).isEqualTo(Duration.ofMillis(1000 + 2000 * 3 + 500 + 1000 + 2000));
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

    @Test
    void clientAuth() {
        String key = """
            -----BEGIN PRIVATE KEY-----
            MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDRsxnfQ5w9YvfB
            opPYIyiSoqlHdFjHgMc7KiQZkObw+dQDFRru5heUABQkNJmX6AwNac2ZUsyVKJzJ
            Ogs7vC7maDowjbX/dPueJOAvcQJephhI696ktQUI9SPrUEBjB1yNZPGuw6CzHf1Q
            0WIWBz0zJdrKKNtiGaVBJVV080eJqXNqVNvxE2qCPapBorHzXum7PJJ6S4A6nduJ
            AbW07eTS9cpKaY/w+UUstaQxh0DrBlpk+r7kwc0rvGPk5IqA2lpXFXB4J1CeP4zU
            XcCyuuILOjAhW0s/fbvng7RBEsRw9Sr/I1GHmEAip38auuo0Jo6LK/exEJS1cPOt
            3pn6ZOJHAgMBAAECggEBAI1GFBAOBJx2qGOTm/bo+NSRMWoqQAVoBNEfOuLedDe0
            l9jbxyFbclcLFoatySxF8Ji4xwKcfaefjMEkkZzU0uP3I/zB+/L2ZO3N+hr8D5Sk
            YYf/ICfmrFW0kVZe4oHU282GTCxpL0J82cQwy9v7VIe44QWBMQuOK38QH9qCa5eZ
            kH1kEIyS7cATq7HUAtwpQXMBasru8pcF/mvxrl9Kxo2HR1Ft0uuk5YxU77OwhMGN
            er90vEtgTwGlNuOiYP6dBCqXjaoUkK7y+vWdC4MADt3Yb92ZL5x3MNSOUgaNK4n/
            gdIIi8SX3rUm0MvYPbi5Edyovvxj/oKTXIeN1boZfQECgYEA964L5RhC1CNJWaF1
            N91kgxgd+I/KREhrPjaI6kXd/Ipx2BDfOrFFMJ/r9RScjkXahJ9kkFbI5ODbmq2J
            tc5tQ806qKZyICxNtwvePJ5ofPX4vuBFgTDcFOndunZNOSE8Z9hKURF/icMtuMks
            5kP2eIQ9MR7B1BE7Oii90Y3jgZECgYEA2L5wT5QBXwQT1cQI/a4BR6CNPiQPDFH6
            0/PkTVpp//v+DORkyDqx5Kxcm+flr4Z/ZXHRZN77Bvz5Ctis3AUkeqPa6w/9fN4X
            /8Ikr+nEEv5Miq9KUENg3irg/GhkrgMrIEjJtRoX83rDuYA2VpdSHihcgIADuxVh
            Ad0zKs6pOlcCgYEA4KkY1Qd6pt4TDKAtqSzCnT7lAK+88s8Vp+rjqk0RnJ8fwLMd
            KSne7spAx5+Ymly2Z5IFL//oXeOwjs9WmHjUF6oyyQOhhZlqLN0xCp9Ne/vJU6ou
            oY7mMnony5i/V1DUcAV686oBm75U3MZuWbfP+2VH9CRIAMpYhNqrw0cG1GECgYAM
            8OnxxmuCidLJW6gRxXbsGOj2Ad8oJCjuyMwj1jcoApq7mFNwJRkEB0qMQH9VSJmI
            rBANjsa8NxSDkkZX6LKx4+CpUk4XS4dxTN0156Y+YBKf4TF1s2AwQ60lRPJl9Wh4
            x//LIWC2t+jBxab46pYLmOtU/M+8Nt2Q5+0rLCWshwKBgHbJZtUaOTuoA6uk+czS
            PW5mCQ6uKEDVBuX9KoP4Qnhya45ffVgwU+IpQKXIL2VFvWoUCe+DNlkAvYIF2TgS
            ueWfQ5sH0zt4kVRO3u+sKL0bvgVHEaSy0PWeBa8cUsGJoafoyQSycZ8Rovnufs4x
            vhuWMU79/9YMyMFMcpYmOo8V
            -----END PRIVATE KEY-----
            """;
        String cert = """
            -----BEGIN CERTIFICATE-----
            MIICsDCCAZgCCQDw6QNdD9G12TANBgkqhkiG9w0BAQsFADAaMQswCQYDVQQGEwJV
            UzELMAkGA1UECAwCTlkwHhcNMjEwOTAyMDExNDA4WhcNMzEwODMxMDExNDA4WjAa
            MQswCQYDVQQGEwJVUzELMAkGA1UECAwCTlkwggEiMA0GCSqGSIb3DQEBAQUAA4IB
            DwAwggEKAoIBAQDRsxnfQ5w9YvfBopPYIyiSoqlHdFjHgMc7KiQZkObw+dQDFRru
            5heUABQkNJmX6AwNac2ZUsyVKJzJOgs7vC7maDowjbX/dPueJOAvcQJephhI696k
            tQUI9SPrUEBjB1yNZPGuw6CzHf1Q0WIWBz0zJdrKKNtiGaVBJVV080eJqXNqVNvx
            E2qCPapBorHzXum7PJJ6S4A6nduJAbW07eTS9cpKaY/w+UUstaQxh0DrBlpk+r7k
            wc0rvGPk5IqA2lpXFXB4J1CeP4zUXcCyuuILOjAhW0s/fbvng7RBEsRw9Sr/I1GH
            mEAip38auuo0Jo6LK/exEJS1cPOt3pn6ZOJHAgMBAAEwDQYJKoZIhvcNAQELBQAD
            ggEBAMkRl0eRZXYLnnJYDYIxudKADXGCeYZ7mV+Mgu0SY0ODRzzkTt1HB8rj9jrC
            3fkwRcxiEFuvVIYfc1KI/k1yMi1IUw19Vypnb1d6MZtNAUID5pv0AHot4TW0Q/Rp
            sG12K1vfgO/O2fCqi0ppz6VuwmFtuv31dxPX+PkKUE1rdS+d164jRivQENDaBKyz
            y6NfNPI7zHU2H40bHKMxcTGIss8Vfw4kEj2RWI7NIMxvMHzSWGRddjOzazWo+d9c
            fa1ZgirHE5R7oiBGr/nehRb1dUFn76jsRCqcrWfiB0T1MyPfg9d8tPv5ubhoLu5v
            X9WSd5xy+qsV6omLIJGCFHP2VWs=
            -----END CERTIFICATE-----
            """;
        builder.clientAuth(key, cert).build();
    }
}
