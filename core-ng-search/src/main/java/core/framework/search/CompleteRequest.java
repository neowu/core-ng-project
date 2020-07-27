package core.framework.search;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author neo
 */
public class CompleteRequest {
    @Nullable
    public String index;
    public String prefix;
    public List<String> fields;
    @Nullable
    public Integer limit;   // limit per field, e.g. limit=5 with 2 fields, may return max 10 suggestions
}
