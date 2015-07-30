package core.framework.api.web.site;

import core.framework.api.web.Request;

import java.util.Optional;

/**
 * @author neo
 */
public interface MessageProvider {
    Optional<String> get(String key, Request request);
}
