package core.framework.search;

import javax.annotation.Nullable;

/**
 * @author neo
 */
public class PartialUpdateRequest<T> {
    @Nullable
    public String index;
    public String id;
    public T source;
    @Nullable
    public Integer retryOnConflict; // refer to https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html
}
