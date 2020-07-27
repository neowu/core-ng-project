package core.framework.search;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * @author neo
 */
public class UpdateRequest<T> {
    @Nullable
    public String index;
    public String id;
    public String script;
    @Nullable
    public Map<String, Object> params;
}
