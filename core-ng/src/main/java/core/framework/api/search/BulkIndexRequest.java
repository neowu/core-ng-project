package core.framework.api.search;

import java.util.Map;

/**
 * @author neo
 */
public class BulkIndexRequest<T> {
    public String index;
    public Map<String, T> sources;
}
