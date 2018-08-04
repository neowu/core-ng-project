package core.framework.web.site;

/**
 * @author neo
 */
public interface Message {
    String get(String key, String language);

    default String get(String key) {
        return get(key, null);
    }
}
