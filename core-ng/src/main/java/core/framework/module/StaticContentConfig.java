package core.framework.module;

import core.framework.impl.web.site.StaticContentController;

import java.time.Duration;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public final class StaticContentConfig {
    private final StaticContentController controller;

    StaticContentConfig(StaticContentController controller) {
        this.controller = controller;
    }

    public void cache(Duration maxAge) {
        if (maxAge == null || maxAge.getSeconds() <= 0) throw new Error(format("maxAge must be greater than 0, maxAge={}", maxAge));
        controller.cache(maxAge);
    }
}
