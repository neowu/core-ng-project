package core.framework.internal.db.cloud;

/**
 * @author neo
 */
public interface CloudAuthProvider {
    String user();

    String accessToken();
}
