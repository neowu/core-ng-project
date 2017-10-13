package core.framework.web;

import java.util.Optional;

/**
 * @author neo
 */
public interface ErrorHandler {
    Optional<Response> handle(Request request, Throwable e);
}
