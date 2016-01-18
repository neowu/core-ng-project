package app.web;

import core.framework.api.web.CookieSpec;

import java.time.Duration;

/**
 * @author neo
 */
public class Cookies {
    public static final CookieSpec TEST = new CookieSpec("test").httpOnly().sessionScope();
    public static final CookieSpec TEST1 = new CookieSpec("test1").httpOnly().maxAge(Duration.ofHours(2));
}
