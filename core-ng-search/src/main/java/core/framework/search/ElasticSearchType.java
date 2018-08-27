package core.framework.search;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author neo
 */
public interface ElasticSearchType<T> {
    SearchResponse<T> search(SearchRequest request);

    List<String> complete(CompleteRequest request);

    default List<String> complete(String prefix, String... fields) {
        CompleteRequest request = new CompleteRequest();
        request.prefix = prefix;
        request.fields = List.of(fields);
        return complete(request);
    }

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

    void update(UpdateRequest<T> request);

    default void update(String id, String script) {
        UpdateRequest<T> request = new UpdateRequest<>();
        request.id = id;
        request.script = script;
        update(request);
    }

    boolean delete(DeleteRequest request);

    default boolean delete(String id) {
        DeleteRequest request = new DeleteRequest();
        request.id = id;
        return delete(request);
    }

    void bulkDelete(BulkDeleteRequest request);

    default void bulkDelete(List<String> ids) {
        BulkDeleteRequest request = new BulkDeleteRequest();
        request.ids = ids;
        bulkDelete(request);
    }

    List<String> analyze(AnalyzeRequest request);   // can be used to test customized analyzer

    default List<String> analyze(String analyzer, String text) {
        AnalyzeRequest request = new AnalyzeRequest();
        request.analyzer = analyzer;
        request.text = text;
        return analyze(request);
    }

    void forEach(ForEach<T> forEach);
}
