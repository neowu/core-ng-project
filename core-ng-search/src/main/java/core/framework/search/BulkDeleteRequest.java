package core.framework.search;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author neo
 */
public class BulkDeleteRequest {
    @Nullable
    public String index;
    public List<String> ids;
}
