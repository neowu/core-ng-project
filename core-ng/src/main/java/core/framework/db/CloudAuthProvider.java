package core.framework.db;

/**
 * @author neo
 */
public interface CloudAuthProvider {
    String user();

    String accessToken();

    class Provider {
        // used by driver to determine if current connection requires cloud auth, for multiple connections with different auth
        public static final String CLOUD_AUTH = "core.framework.db.cloudAuth";
        // in cloud env, only need one global auth provider
        private static CloudAuthProvider provider;

        public static void set(CloudAuthProvider provider) {
            if (Provider.provider != null) throw new Error("provider is set, provider=" + Provider.provider);
            Provider.provider = provider;
        }

        public static CloudAuthProvider get() {
            return provider;
        }
    }
}
