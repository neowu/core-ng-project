package core.framework.mongo;

import com.mongodb.ReadPreference;
import org.jspecify.annotations.Nullable;

/**
 * @author neo
 */
public final class Get {
    public Object id;
    @Nullable
    public ReadPreference readPreference;
}
