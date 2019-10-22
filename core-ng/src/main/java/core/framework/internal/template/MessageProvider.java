package core.framework.internal.template;

import java.util.Optional;

/**
 * @author neo
 */
public interface MessageProvider {
    Optional<String> get(String key);
}
