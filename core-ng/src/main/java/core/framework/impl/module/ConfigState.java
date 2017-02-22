package core.framework.impl.module;

import core.framework.api.module.CacheConfig;
import core.framework.api.module.DBConfig;
import core.framework.api.module.KafkaConfig;
import core.framework.api.module.MongoConfig;
import core.framework.api.module.RedisConfig;
import core.framework.api.module.SchedulerConfig;
import core.framework.api.module.SearchConfig;
import core.framework.api.util.Maps;

import java.util.Map;

/**
 * @author neo
 */
public class ConfigState {
    private RedisConfig.RedisConfigState redis;
    private Map<String, MongoConfig.MongoConfigState> mongo;
    private Map<String, KafkaConfig.KafkaConfigState> kafka;
    private Map<String, DBConfig.DBConfigState> db;
    private CacheConfig.CacheConfigState cache;
    private SearchConfig.SearchConfigState search;
    private SchedulerConfig.SchedulerConfigState scheduler;

    public void validate() {
        if (redis != null) redis.validate();
        if (mongo != null) mongo.values().forEach(MongoConfig.MongoConfigState::validate);
        if (kafka != null) kafka.values().forEach(KafkaConfig.KafkaConfigState::validate);
        if (db != null) db.values().forEach(DBConfig.DBConfigState::validate);
        if (cache != null) cache.validate();
        if (search != null) search.validate();
    }

    public MongoConfig.MongoConfigState mongo(String name) {
        if (mongo == null) mongo = Maps.newHashMap();
        return mongo.computeIfAbsent(name, MongoConfig.MongoConfigState::new);
    }

    public RedisConfig.RedisConfigState redis() {
        if (redis == null) redis = new RedisConfig.RedisConfigState();
        return redis;
    }

    public KafkaConfig.KafkaConfigState kafka(String name) {
        if (kafka == null) kafka = Maps.newHashMap();
        return kafka.computeIfAbsent(name, KafkaConfig.KafkaConfigState::new);
    }

    public DBConfig.DBConfigState db(String name) {
        if (db == null) db = Maps.newHashMap();
        return db.computeIfAbsent(name, DBConfig.DBConfigState::new);
    }

    public CacheConfig.CacheConfigState cache() {
        if (cache == null) cache = new CacheConfig.CacheConfigState();
        return cache;
    }

    public SearchConfig.SearchConfigState search() {
        if (search == null) search = new SearchConfig.SearchConfigState();
        return search;
    }

    public SchedulerConfig.SchedulerConfigState scheduler() {
        if (scheduler == null) scheduler = new SchedulerConfig.SchedulerConfigState();
        return scheduler;
    }
}
