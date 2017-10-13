package core.framework.web;

import java.time.Duration;

/**
 * @author neo
 */
public final class CookieSpec {
    private static final Duration SESSION_SCOPE = Duration.ofSeconds(-1);

    public final String name;
    public String domain;
    public String path;
    public boolean httpOnly;
    public boolean secure;
    public Duration maxAge;

    public CookieSpec(String name) {
        this.name = name;
    }

    public CookieSpec httpOnly() {
        httpOnly = true;
        return this;
    }

    public CookieSpec domain(String domain) {
        this.domain = domain;
        return this;
    }

    public CookieSpec path(String path) {
        this.path = path;
        return this;
    }

    public CookieSpec secure() {
        secure = true;
        return this;
    }

    public CookieSpec maxAge(Duration maxAge) {
        this.maxAge = maxAge;
        return this;
    }

    public CookieSpec sessionScope() {
        this.maxAge = SESSION_SCOPE;
        return this;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        CookieSpec that = (CookieSpec) object;

        return name.equals(that.name)
            && !(domain != null ? !domain.equals(that.domain) : that.domain != null)
            && !(path != null ? !path.equals(that.path) : that.path != null);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (domain != null ? domain.hashCode() : 0);
        result = 31 * result + (path != null ? path.hashCode() : 0);
        return result;
    }
}


