package core.framework.search;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * @author neo
 */
public class UpdateRequest {
    @Nullable
    public String index;
    public String id;
    public String script;
    @Nullable
    public Map<String, Object> params;
    @Nullable
    public Integer retryOnConflict; // refer to https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html
}
