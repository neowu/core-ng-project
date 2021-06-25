package core.framework.web;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.Optional;

/**
 * @author neo
 */
public interface Session {
    Optional<String> get(String key);

    void set(String key, @Nullable String value); // set value to null to remove key

    void invalidate();

    // set timeout for current session, to override default timeout in site().session().timeout()
    // e.g. to extend timeout for specific userAgent or features such as "remember me"
    void timeout(Duration timeout);
}
