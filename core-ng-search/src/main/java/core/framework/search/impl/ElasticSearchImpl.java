package core.framework.search.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.ErrorCause;
import co.elastic.clients.elasticsearch.cluster.state.ClusterStateMetric;
import co.elastic.clients.elasticsearch.indices.ElasticsearchIndicesClient;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.json.jackson.Jackson3JsonpMapper;
import co.elastic.clients.transport.rest5_client.Rest5ClientTransport;
import co.elastic.clients.transport.rest5_client.low_level.Request;
import co.elastic.clients.transport.rest5_client.low_level.Response;
import co.elastic.clients.transport.rest5_client.low_level.Rest5Client;
import co.elastic.clients.transport.rest5_client.low_level.Rest5ClientBuilder;
import com.fasterxml.jackson.annotation.JsonInclude;
import core.framework.internal.json.JSONMapper;
import core.framework.log.ActionLogContext;
import core.framework.search.ClusterStateResponse;
import core.framework.search.ElasticSearch;
import core.framework.search.ElasticSearchType;
import core.framework.search.SearchException;
import core.framework.util.StopWatch;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.util.Timeout;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public class ElasticSearchImpl implements ElasticSearch {
    public final LogInstrumentation instrumentation = new LogInstrumentation();
    private final Logger logger = LoggerFactory.getLogger(ElasticSearchImpl.class);

    public Duration timeout = Duration.ofSeconds(15);
    public int maxConnections = 200;
    public HttpHost[] hosts;
    public int maxResultWindow = 10000;

    ElasticsearchClient client;
    @Nullable
    Header authHeader;
    private Rest5Client restClient;
    private Jackson3JsonpMapper mapper;

    // initialize will be called in startup hook, no need to synchronize
    public void initialize() {
        if (client == null) {   // initialize can be called by initSearch explicitly during test,
            Rest5ClientBuilder builder = Rest5Client.builder(hosts);    // setCompressionEnabled(true) slowed down performance significantly
            if (authHeader != null) {
                builder.setDefaultHeaders(new Header[]{authHeader});
            }
            builder.setConnectionConfigCallback(config -> config.setConnectTimeout(Timeout.ofSeconds(5)));    // 5s, usually es is within same network, use shorter timeout to fail fast
            builder.setRequestConfigCallback(config -> config.setConnectionRequestTimeout(Timeout.ofSeconds(timeout.toSeconds()))    // timeout of requesting connection from connection pool
                .setResponseTimeout(Timeout.of(timeout)));
            // default is too low, generally all requests are sent to same es route
            // es operations usually happen in virtual threads, set max connection according to desired concurrency
            builder.setConnectionManagerCallback(config -> config.setMaxConnPerRoute(maxConnections)
                .setMaxConnTotal(maxConnections)
                .setPoolConcurrencyPolicy(PoolConcurrencyPolicy.LAX));
            restClient = builder.build();
            mapper = new Jackson3JsonpMapper(JSONMapper.builder()
                // only include not null fields for partial update
                .changeDefaultPropertyInclusion(_ -> JsonInclude.Value.construct(JsonInclude.Include.NON_NULL, JsonInclude.Include.NON_NULL))
                .build());
            client = new ElasticsearchClient(new Rest5ClientTransport(restClient, mapper, null, instrumentation));
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

    // refer to co.elastic.clients.transport.rest5_client.Rest5ClientTransport.buildRest5Client
    public void auth(String apiKey) {
        if (apiKey == null) throw new Error("apiKey must not be null");
        authHeader = new BasicHeader("Authorization", "ApiKey " + apiKey);
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
                request.setJsonEntity(mapper.objectMapper().readTree(source).get("mappings").toString());
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
            return client.cluster().state(builder -> builder.metric(List.of(ClusterStateMetric.Metadata))).state().to(ClusterStateResponse.class);
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
