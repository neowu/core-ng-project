package core.framework.search;

import java.util.List;

/**
 * @author neo
 */
public class CompleteRequest {
    public String index;
    public String prefix;
    public List<String> fields;
    public Integer limit;   // limit per field, e.g. limit=5 with 2 fields, may return max 10 suggestions
}
