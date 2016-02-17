package core.framework.api.search;

import java.util.Map;
import java.util.Optional;

/**
 * @author neo
 */
public interface ElasticSearchType<T> {
    SearchResponse<T> search(SearchRequest request);

    Optional<T> get(GetRequest request);

    default Optional<T> get(String id) {
        GetRequest request = new GetRequest();
        request.id = id;
        return get(request);
    }

    void index(IndexRequest<T> request);

    default void index(String id, T source) {
        IndexRequest<T> request = new IndexRequest<>();
        request.id = id;
        request.source = source;
        index(request);
    }

    void bulkIndex(BulkIndexRequest<T> request);

    default void bulkIndex(Map<String, T> sources) {
        BulkIndexRequest<T> request = new BulkIndexRequest<>();
        request.sources = sources;
        bulkIndex(request);
    }

    boolean delete(DeleteRequest request);

    default boolean delete(String id) {
        DeleteRequest request = new DeleteRequest();
        request.id = id;
        return delete(request);
    }
}
