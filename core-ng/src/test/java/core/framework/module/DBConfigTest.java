package core.framework.module;

import core.framework.internal.db.cloud.AzureAuthProvider;
import core.framework.internal.db.cloud.GCloudAuthProvider;
import core.framework.internal.module.ModuleContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class DBConfigTest {
    private DBConfig config;

    @BeforeEach
    void createDBConfig() {
        config = new DBConfig();
        config.initialize(new ModuleContext(null), null);
    }

    @Test
    void validate() {
        assertThatThrownBy(config::validate)
            .hasMessageContaining("db url must be configured");

        config.url("jdbc:hsqldb:mem:.");
        assertThatThrownBy(config::validate)
            .hasMessageContaining("db is configured but no repository/view added");
    }

    @Test
    void provider() {
        assertThat(config.provider("iam/azure/some-service"))
            .isInstanceOf(AzureAuthProvider.class)
            .satisfies(provider -> assertThat(provider.user()).isEqualTo("some-service"));
        assertThatThrownBy(() -> config.provider("iam/azure/")).hasMessageStartingWith("invalid azure iam user");

        assertThat(config.provider("iam/gcloud")).isInstanceOf(GCloudAuthProvider.class);
    }
}
