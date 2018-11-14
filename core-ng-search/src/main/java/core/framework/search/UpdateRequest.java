package core.framework.search;

import java.util.Map;

/**
 * @author neo
 */
public class UpdateRequest<T> {
    public String index;
    public String id;
    public String script;
    public Map<String, Object> params;
}
