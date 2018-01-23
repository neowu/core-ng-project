package core.framework.impl.web.site;

import core.framework.web.Controller;

import java.time.Duration;

/**
 * @author neo
 */
public interface StaticContentController extends Controller {
    void cache(Duration maxAge);
}
