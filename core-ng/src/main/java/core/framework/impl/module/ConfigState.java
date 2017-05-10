package core.framework.impl.module;

import core.framework.api.module.APIConfig;
import core.framework.api.module.CacheConfig;
import core.framework.api.module.DBConfig;
import core.framework.api.module.HTTPConfig;
import core.framework.api.module.KafkaConfig;
import core.framework.api.module.MongoConfig;
import core.framework.api.module.RedisConfig;
import core.framework.api.module.SchedulerConfig;
import core.framework.api.module.SearchConfig;
import core.framework.api.module.SiteConfig;
import core.framework.api.util.Maps;

import java.util.Map;

/**
 * @author neo
 */
public class ConfigState {
    private RedisConfig.State redis;
    private Map<String, MongoConfig.State> mongo;
    private Map<String, KafkaConfig.State> kafka;
    private Map<String, DBConfig.State> db;
    private CacheConfig.State cache;
    private SearchConfig.State search;
    private SchedulerConfig.State scheduler;
    private SiteConfig.State site;
    private HTTPConfig.State http;
    private APIConfig.State api;

    public void validate() {
        if (redis != null) redis.validate();
        if (mongo != null) mongo.values().forEach(MongoConfig.State::validate);
        if (kafka != null) kafka.values().forEach(KafkaConfig.State::validate);
        if (db != null) db.values().forEach(DBConfig.State::validate);
        if (cache != null) cache.validate();
        if (search != null) search.validate();
        if (http != null) http.validate();
    }

    public MongoConfig.State mongo(String name) {
        if (mongo == null) mongo = Maps.newHashMap();
        return mongo.computeIfAbsent(name, MongoConfig.State::new);
    }

    public RedisConfig.State redis() {
        if (redis == null) redis = new RedisConfig.State();
        return redis;
    }

    public KafkaConfig.State kafka(String name) {
        if (kafka == null) kafka = Maps.newHashMap();
        return kafka.computeIfAbsent(name, KafkaConfig.State::new);
    }

    public DBConfig.State db(String name) {
        if (db == null) db = Maps.newHashMap();
        return db.computeIfAbsent(name, DBConfig.State::new);
    }

    public CacheConfig.State cache() {
        if (cache == null) cache = new CacheConfig.State();
        return cache;
    }

    public SearchConfig.State search() {
        if (search == null) search = new SearchConfig.State();
        return search;
    }

    public SchedulerConfig.State scheduler() {
        if (scheduler == null) scheduler = new SchedulerConfig.State();
        return scheduler;
    }

    public SiteConfig.State site() {
        if (site == null) site = new SiteConfig.State();
        return site;
    }

    public HTTPConfig.State http() {
        if (http == null) http = new HTTPConfig.State();
        return http;
    }

    public APIConfig.State api() {
        if (api == null) api = new APIConfig.State();
        return api;
    }
}
