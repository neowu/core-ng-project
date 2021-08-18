package core.framework.search.impl;

import core.framework.json.JSON;
import core.framework.search.ClusterStateResponse;
import core.framework.search.ElasticSearch;
import core.framework.search.ElasticSearchType;
import core.framework.util.StopWatch;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.action.support.ActiveShardCount;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CloseIndexRequest;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.PutComposableIndexTemplateRequest;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.cluster.metadata.ComposableIndexTemplate;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.xcontent.DeprecationHandler;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author neo
 */
public class ElasticSearchImpl implements ElasticSearch {
    private final Logger logger = LoggerFactory.getLogger(ElasticSearchImpl.class);
    public Duration timeout = Duration.ofSeconds(10);
    public Duration slowOperationThreshold = Duration.ofSeconds(5);
    public HttpHost[] hosts;
    public ElasticSearchAuth auth;
    public int maxResultWindow = 10000;
    private RestHighLevelClient client;

    // initialize will be called in startup hook, so no need to synchronize
    public void initialize() {
        RestClientBuilder builder = RestClient.builder(hosts);
        if (auth != null) {
            Header[] authHeader = {new BasicHeader("Authorization", "ApiKey " + apiKeyAuth())};
            builder.setDefaultHeaders(authHeader);
        }
        builder.setRequestConfigCallback(config -> config.setSocketTimeout((int) timeout.toMillis())
            .setConnectionRequestTimeout((int) timeout.toMillis())); // timeout of requesting connection from connection pool
        builder.setHttpClientConfigCallback(config -> config.setMaxConnTotal(100)
            .setMaxConnPerRoute(100)
            .setKeepAliveStrategy((response, context) -> Duration.ofSeconds(30).toMillis()));
        client = new RestHighLevelClient(builder);
    }

    public <T> ElasticSearchType<T> type(Class<T> documentClass) {
        var watch = new StopWatch();
        try {
            new DocumentClassValidator(documentClass).validate();
            return new ElasticSearchTypeImpl<>(this, documentClass);
        } finally {
            logger.info("register elasticsearch type, documentClass={}, elapsed={}", documentClass.getCanonicalName(), watch.elapsed());
        }
    }

    public void close() throws IOException {
        if (client == null) return;

        logger.info("close elasticsearch client, host={}", hosts[0]);
        client.close();
    }

    // this is generally used in es migration, to create index or update mapping if index exists, be aware of mapping fields can't be deleted in es, but can be removed from mapping json
    @Override
    public void putIndex(String index, String source) {
        var watch = new StopWatch();
        try {
            IndicesClient client = client().indices();
            CreateIndexRequest request = new CreateIndexRequest(index).source(new BytesArray(source), XContentType.JSON);
            boolean exists = client.exists(new GetIndexRequest(index), RequestOptions.DEFAULT);
            if (!exists) {
                client.create(request, RequestOptions.DEFAULT);
            } else {
                // only try to update mappings, as for settings it generally requires to close index first then open after update
                logger.info("index already exists, update mapping, index={}", index);
                client.putMapping(new PutMappingRequest(index).source(request.mappings(), XContentType.JSON), RequestOptions.DEFAULT);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            logger.info("put index, index={}, source={}, elapsed={}", index, source, watch.elapsed());
        }
    }

    @Override
    public void putIndexTemplate(String name, String source) {
        var watch = new StopWatch();
        try {
            XContentParser parser = XContentType.JSON.xContent().createParser(NamedXContentRegistry.EMPTY, DeprecationHandler.THROW_UNSUPPORTED_OPERATION, source);
            client().indices().putIndexTemplate(new PutComposableIndexTemplateRequest().name(name).indexTemplate(ComposableIndexTemplate.parse(parser)), RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            logger.info("put index template, name={}, elapsed={}", name, watch.elapsed());
        }
    }

    @Override
    public void refreshIndex(String index) {
        var watch = new StopWatch();
        try {
            client().indices().refresh(Requests.refreshRequest(index), RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            logger.info("refresh index, index={}, elapsed={}", index, watch.elapsed());
        }
    }

    @Override
    public void closeIndex(String index) {
        var watch = new StopWatch();
        try {
            client().indices().close(new CloseIndexRequest(index).waitForActiveShards(ActiveShardCount.NONE), RequestOptions.DEFAULT);
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
            return JSON.fromJSON(ClusterStateResponse.class, new String(bytes, UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            logger.info("get cluster state, elapsed={}", watch.elapsed());
        }
    }

    private String apiKeyAuth() {
        return Base64.getEncoder().encodeToString(
            (auth.apiKeyId + ":" + auth.apiSecret).getBytes(UTF_8)
        );
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
