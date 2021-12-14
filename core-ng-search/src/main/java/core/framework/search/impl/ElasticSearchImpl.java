package core.framework.search.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.ElasticsearchIndicesClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import core.framework.internal.json.JSONMapper;
import core.framework.search.ClusterStateResponse;
import core.framework.search.ElasticSearch;
import core.framework.search.ElasticSearchType;
import core.framework.util.StopWatch;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;

/**
 * @author neo
 */
public class ElasticSearchImpl implements ElasticSearch {
    private final Logger logger = LoggerFactory.getLogger(ElasticSearchImpl.class);
    public Duration timeout = Duration.ofSeconds(10);
    public Duration slowOperationThreshold = Duration.ofSeconds(5);
    public HttpHost[] hosts;
    public int maxResultWindow = 10000;
    ElasticsearchClient client;
    private RestClient restClient;

    // initialize will be called in startup hook, no need to synchronize
    public void initialize() {
        if (client == null) {   // initialize can be called by initSearch explicitly during test,
            RestClientBuilder builder = RestClient.builder(hosts);
            builder.setRequestConfigCallback(config -> config.setSocketTimeout((int) timeout.toMillis())
                    .setConnectionRequestTimeout((int) timeout.toMillis())); // timeout of requesting connection from connection pool
            builder.setHttpClientConfigCallback(config -> config.setMaxConnTotal(100)
                    .setMaxConnPerRoute(100)
                    .setKeepAliveStrategy((response, context) -> Duration.ofSeconds(30).toMillis()));
            builder.setHttpClientConfigCallback(config -> config.addInterceptorFirst(new ElasticSearchLogInterceptor()));
            restClient = builder.build();
            client = new ElasticsearchClient(new RestClientTransport(restClient, new JacksonJsonpMapper(JSONMapper.OBJECT_MAPPER)));
        }
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
        restClient.close(); // same as client._transport().close()
    }

    // this is generally used in es migration, to create index or update mapping if index exists, be aware of mapping fields can't be deleted in es, but can be removed from mapping json
    @Override
    public void putIndex(String index, String source) {
        var watch = new StopWatch();
        HttpEntity entity = null;
        try {
            ElasticsearchIndicesClient client = this.client.indices();
            boolean exists = client.exists(builder -> builder.index(index)).value();
            Request request;
            if (!exists) {
                request = new Request("PUT", "/" + index);
                request.setJsonEntity(source);
            } else {
                // only try to update mappings, as for settings it generally requires closing index first then open after update
                logger.info("index already exists, update mapping, index={}", index);
                request = new Request("PUT", "/" + index + "/_mapping");
                request.setJsonEntity(JSONMapper.OBJECT_MAPPER.readTree(source).get("mappings").toString());
            }
            Response response = restClient.performRequest(request);
            entity = response.getEntity();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            EntityUtils.consumeQuietly(entity);
            logger.info("put index, index={}, source={}, elapsed={}", index, source, watch.elapsed());
        }
    }

    @Override
    public void putIndexTemplate(String name, String source) {
        var watch = new StopWatch();
        HttpEntity entity = null;
        try {
            var request = new Request("PUT", "/_index_template/" + name);
            request.setJsonEntity(source);
            Response response = restClient.performRequest(request);
            entity = response.getEntity();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            EntityUtils.consumeQuietly(entity);
            logger.info("put index template, name={}, source={}, elapsed={}", name, source, watch.elapsed());
        }
    }

    @Override
    public void refreshIndex(String index) {
        var watch = new StopWatch();
        try {
            client.indices().refresh(builder -> builder.index(index));
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
            client.indices().close(builder -> builder.index(index).waitForActiveShards(w -> w.count(0)));
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
            client.indices().delete(builder -> builder.index(index));
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
            return client.cluster().state(builder -> builder.metric("metadata")).valueBody().to(ClusterStateResponse.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            logger.info("get cluster state, elapsed={}", watch.elapsed());
        }
    }
}
