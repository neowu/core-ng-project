package core.framework.internal.http;

import core.framework.util.Maps;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public class CookieManager implements CookieJar {
    final Map<String, Cookie> store = Maps.newConcurrentHashMap();

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        for (Cookie cookie : cookies) {
            String key = cookie.domain() + ":" + cookie.path() + ":" + cookie.name();
            // refer to okhttp3.Cookie.parse(), with maxAge=0, it set expiresAt = Long.MIN_VALUE
            if (cookie.expiresAt() == Long.MIN_VALUE && "".equals(cookie.value())) {
                store.remove(key);
            } else {
                store.put(key, cookie);
            }
        }
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        List<Cookie> matchingCookies = new ArrayList<>();
        Iterator<Map.Entry<String, Cookie>> iterator = store.entrySet().iterator();
        long now = System.currentTimeMillis();
        while (iterator.hasNext()) {
            Cookie cookie = iterator.next().getValue();
            if (cookie.expiresAt() < now) {
                iterator.remove();
            } else if (cookie.matches(url)) {
                matchingCookies.add(cookie);
            }
        }
        return matchingCookies;
    }
}
