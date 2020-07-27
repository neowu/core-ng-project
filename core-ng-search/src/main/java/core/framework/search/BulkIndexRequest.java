package core.framework.search;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * @author neo
 */
public class BulkIndexRequest<T> {
    @Nullable
    public String index;
    public Map<String, T> sources;
}
