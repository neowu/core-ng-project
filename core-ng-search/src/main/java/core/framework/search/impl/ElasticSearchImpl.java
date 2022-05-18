package core.framework.search.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.ErrorCause;
import co.elastic.clients.elasticsearch.indices.ElasticsearchIndicesClient;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.framework.internal.json.JSONMapper;
import core.framework.log.ActionLogContext;
import core.framework.search.ClusterStateResponse;
import core.framework.search.ElasticSearch;
import core.framework.search.ElasticSearchType;
import core.framework.search.SearchException;
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
import java.util.Map;

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
            ObjectMapper mapper = JSONMapper.OBJECT_MAPPER.copy().setSerializationInclusion(JsonInclude.Include.NON_NULL);
            client = new ElasticsearchClient(new RestClientTransport(restClient, new JacksonJsonpMapper(mapper)));
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
        } catch (ElasticsearchException e) {
            throw searchException(e);
        } finally {
            long elapsed = watch.elapsed();
            ActionLogContext.track("elasticsearch", elapsed);
            logger.info("refresh index, index={}, elapsed={}", index, elapsed);
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
            client.indices().delete(builder -> builder.index(index));
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
            return client.cluster().state(builder -> builder.metric("metadata")).valueBody().to(ClusterStateResponse.class);
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
