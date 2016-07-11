package core.framework.api.search;

import java.util.List;

/**
 * @author neo
 */
public class BulkDeleteRequest {
    public String index;
    public List<String> ids;
}
