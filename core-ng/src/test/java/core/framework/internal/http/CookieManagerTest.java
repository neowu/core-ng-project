package core.framework.internal.http;

import okhttp3.Cookie;
import okhttp3.HttpUrl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CookieManagerTest {
    private CookieManager cookieManager;

    @BeforeEach
    void createCookieManager() {
        cookieManager = new CookieManager();
    }

    @Test
    void saveFromResponse() {
        HttpUrl url = HttpUrl.get("http://localhost/");
        Cookie cookie1 = Cookie.parse(url, "key1=v1; Path=/;"); // session scope
        Cookie cookie2 = Cookie.parse(url, "key2=v2; Domain=localhost; Max-Age=7200;");
        cookieManager.saveFromResponse(url, List.of(cookie1, cookie2));

        assertThat(cookieManager.store).hasSize(2);

        cookie1 = Cookie.parse(url, "key1=; Path=/; Max-Age=0;");
        cookieManager.saveFromResponse(url, List.of(cookie1));

        assertThat(cookieManager.store).hasSize(1);
    }

    @Test
    void loadForRequest() {
        HttpUrl url = HttpUrl.get("http://localhost/");
        Cookie cookie1 = Cookie.parse(url, "key1=v1; Path=/;");
        Cookie cookie2 = Cookie.parse(url, "key2=v2; Domain=localhost; Max-Age=0;");
        cookieManager.store.put("localhost:/:key1", cookie1);
        cookieManager.store.put("localhost:/:key2", cookie2);

        List<Cookie> cookies = cookieManager.loadForRequest(url);
        assertThat(cookies).hasSize(1);

        assertThat(cookieManager.store).hasSize(1);
    }
}
