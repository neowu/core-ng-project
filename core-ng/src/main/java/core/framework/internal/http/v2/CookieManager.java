package core.framework.internal.http.v2;

import core.framework.util.Sets;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author neo
 */
public class CookieManager implements CookieJar {
    private final Set<Cookie> store = Sets.newConcurrentHashSet();

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        store.addAll(cookies);
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        List<Cookie> matchingCookies = new ArrayList<>();
        Iterator<Cookie> iterator = store.iterator();
        while (iterator.hasNext()) {
            Cookie cookie = iterator.next();
            if (cookie.expiresAt() < System.currentTimeMillis()) {
                iterator.remove();
            } else if (cookie.matches(url)) {
                matchingCookies.add(cookie);
            }
        }
        return matchingCookies;
    }
}
