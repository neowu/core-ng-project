package core.framework.search;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * @author miller
 */
public class BulkIndexWithRoutingRequest<T> {
    @Nullable
    public String index;
    public Map<String, Request<T>> requests;

    public static class Request<T> {
        public String routing;
        public T source;
    }
}
