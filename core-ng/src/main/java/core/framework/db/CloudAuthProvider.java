package core.framework.db;

/**
 * @author neo
 */
public interface CloudAuthProvider {
    String user();

    String accessToken();

    class Registry {
        // used by driver to determine if current connection requires cloud auth, for multiple connections with different auth
        public static final String KEY = "core.framework.db.authProvider";
        // in cloud env, only need one global auth provider
        private static CloudAuthProvider provider;

        public static void register(CloudAuthProvider provider) {
            if (Registry.provider != null) throw new Error("provider is registered, provider=" + Registry.provider);
            Registry.provider = provider;
        }

        public static CloudAuthProvider get() {
            return provider;
        }
    }
}
