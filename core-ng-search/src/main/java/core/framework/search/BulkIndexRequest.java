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
    @Nullable
    public Boolean refresh; // whether refresh index after operation, by default, changes only visible after index settings->refresh_interval
}
