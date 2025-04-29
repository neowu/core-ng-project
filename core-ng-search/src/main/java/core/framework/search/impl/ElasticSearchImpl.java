package core.framework.search.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.ErrorCause;
import co.elastic.clients.elasticsearch.indices.ElasticsearchIndicesClient;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest5_client.Rest5ClientTransport;
import co.elastic.clients.transport.rest5_client.low_level.Request;
import co.elastic.clients.transport.rest5_client.low_level.Response;
import co.elastic.clients.transport.rest5_client.low_level.Rest5Client;
import co.elastic.clients.transport.rest5_client.low_level.Rest5ClientBuilder;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.framework.internal.json.JSONMapper;
import core.framework.log.ActionLogContext;
import core.framework.search.ClusterStateResponse;
import core.framework.search.ElasticSearch;
import core.framework.search.ElasticSearchType;
import core.framework.search.SearchException;
import core.framework.util.Encodings;
import core.framework.util.StopWatch;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author neo
 */
public class ElasticSearchImpl implements ElasticSearch {
    private final Logger logger = LoggerFactory.getLogger(ElasticSearchImpl.class);

    public Duration timeout = Duration.ofSeconds(15);
    public HttpHost[] hosts;
    public int maxResultWindow = 10000;
    ElasticsearchClient client;
    Header authHeader;
    private Rest5Client restClient;
    private ObjectMapper mapper;

    // initialize will be called in startup hook, no need to synchronize
    public void initialize() {
        if (client == null) {
            // initialize can be called by initSearch explicitly during test,
            Rest5ClientBuilder builder = Rest5Client.builder(hosts);
            if (authHeader != null) {
                builder.setDefaultHeaders(new Header[]{authHeader});
            }

            RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofSeconds(5))
                .setResponseTimeout((int) timeout.toMillis(), TimeUnit.MILLISECONDS)
                .build();

            ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(5))
                .setSocketTimeout((int) timeout.toMillis(), TimeUnit.MILLISECONDS)
                .setTimeToLive(TimeValue.ofSeconds(30))
                .build();

            PoolingAsyncClientConnectionManager connectionManager =
                PoolingAsyncClientConnectionManagerBuilder.create()
                    .setDefaultConnectionConfig(connectionConfig)
                    .setMaxConnPerRoute(100)
                    .setMaxConnTotal(100)
                    .build();

            HttpAsyncClientBuilder httpClientBuilder = HttpAsyncClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(connectionManager)
                .setUserAgent("elasticsearch-java/core-ng")
                .setThreadFactory(Thread.ofPlatform().name("elasticsearch-rest-client-", 0).factory());

            builder.setHttpClient(httpClientBuilder.build());

            restClient = builder.build();
            mapper = JSONMapper.builder().serializationInclusion(JsonInclude.Include.NON_NULL).build();
            client = new ElasticsearchClient(new Rest5ClientTransport(restClient, new JacksonJsonpMapper(mapper), null, new ElasticSearchLogInstrumentation()));
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

    public void auth(String apiKeyId, String apiKeySecret) {
        if (apiKeyId == null) throw new Error("apiKeyId must not be null");
        authHeader = new BasicHeader("Authorization", "ApiKey " + Encodings.base64(apiKeyId + ":" + apiKeySecret));
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
                request.setJsonEntity(mapper.readTree(source).get("mappings").toString());
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
        } catch (ElasticsearchException e) {
            throw searchException(e);
        } finally {
            long elapsed = watch.elapsed();
            logger.info("refresh index, index={}, elapsed={}", index, elapsed);
            ActionLogContext.track("elasticsearch", elapsed);
        }
    }

    @Override
    public void closeIndex(String index) {
        var watch = new StopWatch();
        try {
            client.indices().close(builder -> builder.index(index).waitForActiveShards(w -> w.count(0)));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (ElasticsearchException e) {
            throw searchException(e);
        } finally {
            logger.info("close index, index={}, elapsed={}", index, watch.elapsed());
        }
    }

    @Override
    public void deleteIndex(String index) {
        var watch = new StopWatch();
        try {
            client.indices().delete(builder -> builder.index(index).ignoreUnavailable(Boolean.TRUE));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (ElasticsearchException e) {
            throw searchException(e);
        } finally {
            logger.info("delete index, index={}, elapsed={}", index, watch.elapsed());
        }
    }

    @Override
    public ClusterStateResponse state() {
        var watch = new StopWatch();
        try {
            return client.cluster().state(builder -> builder.metric("metadata")).state().to(ClusterStateResponse.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (ElasticsearchException e) {
            throw searchException(e);
        } finally {
            logger.info("get cluster state, elapsed={}", watch.elapsed());
        }
    }

    /*
     convert elasticsearch-java client exception, to append detailed error message
     es put actual reason within metadata, e.g.

     core.framework.search.SearchException: [es/search] failed: [search_phase_execution_exception] all shards failed
     metadata:
     phase="query"
     failed_shards=[{"shard":0,"index":"document","node":"lcqF3AYgTyqBZe7HNApmjg","reason":{"type":"query_shard_exception","reason":"No mapping found for [unexisted] in order to sort on","index_uuid":"vlA9-8zeT2O-aDnnxxnsXA","index":"document"}}]
     grouped=true
    */
    SearchException searchException(ElasticsearchException e) {
        ErrorCause error = e.error();
        var builder = new StringBuilder(e.getMessage());
        builder.append("\nmetadata:\n");
        for (Map.Entry<String, JsonData> entry : error.metadata().entrySet()) {
            builder.append(entry).append('\n');
        }
        ErrorCause causedBy = error.causedBy();
        if (causedBy != null) {
            builder.append("causedBy: ").append(causedBy.reason());
        }
        return new SearchException(builder.toString(), e);
    }
}
