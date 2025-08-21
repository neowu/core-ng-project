package core.framework.db;

import org.jspecify.annotations.Nullable;

/**
 * @author neo
 */
public interface CloudAuthProvider {
    String user(Dialect dialect);   // cloud auth provider is global singleton, one app may connect to multiple cloud databases with different dialects

    String accessToken();

    class Provider {
        // used by driver to determine if current connection requires cloud auth, for multiple connections with different auth
        public static final String CLOUD_AUTH = "core.framework.db.cloudAuth";
        // in cloud env, only need one global auth provider
        @Nullable
        private static CloudAuthProvider provider;

        public static void set(CloudAuthProvider provider) {
            if (Provider.provider != null) throw new Error("provider is set, provider=" + Provider.provider);
            Provider.provider = provider;
        }

        @Nullable
        public static CloudAuthProvider get() {
            return provider;
        }
    }
}
