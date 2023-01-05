package core.framework.internal.web.response;

import io.undertow.io.Sender;

/**
 * @author neo
 */
interface Body {
    // return body length
    long send(Sender sender, ResponseHandlerContext context);
}
