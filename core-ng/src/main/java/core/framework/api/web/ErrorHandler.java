package core.framework.api.web;

import java.util.Optional;

/**
 * @author neo
 */
public interface ErrorHandler {
    Optional<Response> handle(Request request, Throwable e);
}
