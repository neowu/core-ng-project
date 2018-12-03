package core.framework.search.impl;

import core.framework.internal.json.JSONMapper;
import core.framework.search.ClusterStateResponse;
import core.framework.search.ElasticSearch;
import core.framework.search.ElasticSearchType;
import core.framework.util.StopWatch;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateRequest;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.Duration;

/**
 * @author neo
 */
public class ElasticSearchImpl implements ElasticSearch {
    private final Logger logger = LoggerFactory.getLogger(ElasticSearchImpl.class);
    public Duration timeout = Duration.ofSeconds(10);
    public Duration slowOperationThreshold = Duration.ofSeconds(5);
    public String host;
    private RestHighLevelClient client;

    public void initialize() {
        client = new RestHighLevelClient(RestClient.builder(new HttpHost(host, 9200))
                                                   .setRequestConfigCallback(builder -> builder.setSocketTimeout((int) timeout.toMillis())
                                                                                               .setConnectionRequestTimeout((int) timeout.toMillis()))  // timeout of requesting connection from connection pool
                                                   .setHttpClientConfigCallback(builder -> builder.setMaxConnTotal(100).setMaxConnPerRoute(100))
                                                   .setMaxRetryTimeoutMillis((int) timeout.toMillis()));
    }

    public <T> ElasticSearchType<T> type(Class<T> documentClass) {
        var watch = new StopWatch();
        try {
            new DocumentClassValidator(documentClass).validate();
            return new ElasticSearchTypeImpl<>(this, documentClass, slowOperationThreshold);
        } finally {
            logger.info("register elasticsearch type, documentClass={}, elapsed={}", documentClass.getCanonicalName(), watch.elapsed());
        }
    }

    public void close() throws IOException {
        if (client == null) return;

        logger.info("close elasticsearch client, host={}", host);
        client.close();
    }

    @Override
    public void createIndex(String index, String source) {
        var watch = new StopWatch();
        try {
            boolean exists = client().indices().exists(new GetIndexRequest().indices(index), RequestOptions.DEFAULT);
            if (!exists) {
                client().indices().create(Requests.createIndexRequest(index).source(new BytesArray(source), XContentType.JSON), RequestOptions.DEFAULT);
            } else {
                logger.info("index already exists, skip, index={}", index);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            logger.info("create index, index={}, elapsed={}", index, watch.elapsed());
        }
    }

    @Override
    public void createIndexTemplate(String name, String source) {
        var watch = new StopWatch();
        try {
            client().indices().putTemplate(new PutIndexTemplateRequest(name).source(new BytesArray(source), XContentType.JSON), RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            logger.info("create index template, name={}, elapsed={}", name, watch.elapsed());
        }
    }

    @Override
    public void flushIndex(String index) {
        var watch = new StopWatch();
        try {
            client().indices().flush(Requests.flushRequest(index), RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            logger.info("flush index, index={}, elapsed={}", index, watch.elapsed());
        }
    }

    @Override
    public void closeIndex(String index) {
        var watch = new StopWatch();
        try {
            client().indices().close(Requests.closeIndexRequest(index), RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            logger.info("close index, index={}, elapsed={}", index, watch.elapsed());
        }
    }

    @Override
    public void deleteIndex(String index) {
        var watch = new StopWatch();
        try {
            client().indices().delete(Requests.deleteIndexRequest(index), RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            logger.info("delete index, index={}, elapsed={}", index, watch.elapsed());
        }
    }

    @Override
    public ClusterStateResponse state() {
        var watch = new StopWatch();
        try {
            Response response = client().getLowLevelClient().performRequest(new Request("GET", "/_cluster/state/metadata"));
            byte[] bytes = responseBody(response.getEntity());
            JSONMapper<ClusterStateResponse> mapper = new JSONMapper<>(ClusterStateResponse.class);
            return mapper.fromJSON(bytes);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            logger.info("indices, elapsed={}", watch.elapsed());
        }
    }

    private byte[] responseBody(HttpEntity entity) throws IOException {
        try (InputStream stream = entity.getContent()) {
            return stream.readAllBytes();
        }
    }

    RestHighLevelClient client() {
        if (client == null) initialize();
        return client;
    }
}
