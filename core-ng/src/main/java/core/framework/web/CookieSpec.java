package core.framework.web;

import java.time.Duration;
import java.util.Objects;

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
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        CookieSpec spec = (CookieSpec) other;
        return Objects.equals(name, spec.name)
                && Objects.equals(domain, spec.domain)
                && Objects.equals(path, spec.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, domain, path);
    }
}


