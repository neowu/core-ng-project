package core.framework.internal.cache;

import core.framework.internal.json.JSONMapper;
import core.framework.internal.json.JSONReader;
import core.framework.internal.json.JSONWriter;
import core.framework.internal.validate.Validator;

/**
 * @author neo
 */
public class CacheContext<T> {
    final JSONReader<T> reader;
    final JSONWriter<T> writer;
    // only validate when retrieve cache from store, in case data in cache store is stale, e.g. the class structure is changed but still got old data from cache
    // it's opposite as DB, which only validate on save
    final Validator<T> validator;

    CacheContext(Class<T> cacheClass) {
        reader = JSONMapper.reader(cacheClass);
        writer = JSONMapper.writer(cacheClass);
        validator = Validator.of(cacheClass);
    }
}
