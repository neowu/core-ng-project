package core.framework.module;

import core.framework.internal.web.site.StaticContentController;

import java.time.Duration;

/**
 * @author neo
 */
public final class StaticContentConfig {
    private final StaticContentController controller;

    StaticContentConfig(StaticContentController controller) {
        this.controller = controller;
    }

    public void cache(Duration maxAge) {
        if (maxAge == null || maxAge.getSeconds() <= 0) throw new Error("maxAge must be greater than 0, maxAge=" + maxAge);
        controller.cache(maxAge);
    }
}
