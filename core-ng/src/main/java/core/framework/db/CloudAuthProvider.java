package core.framework.db;

/**
 * @author neo
 */
public interface CloudAuthProvider {
    String user();

    String accessToken();

    class Registry {
        public static CloudAuthProvider INSTANCE;
    }
}
