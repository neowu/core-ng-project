package app;

import app.domain.MongoUserAggregateView;
import app.domain.User;
import app.service.UserService;
import core.framework.api.Module;
import core.framework.api.mongo.Mongo;
import core.framework.api.mongo.MongoBuilder;

/**
 * @author neo
 */
public class MongoModule extends Module {
    @Override
    protected void initialize() {
        Mongo mongo = bindSupplier(Mongo.class, null, new MongoBuilder()
            .uri("mongodb://192.168.2.2:27017/main")
            .entityClass(User.class)
            .viewClass(MongoUserAggregateView.class));
        onShutdown(mongo::close);

        bind(UserService.class);
    }
}
