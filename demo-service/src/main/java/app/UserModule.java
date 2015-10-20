package app;

import app.user.domain.MongoUserAggregateView;
import app.user.domain.User;
import app.user.service.UserService;
import core.framework.api.Module;

/**
 * @author neo
 */
public class UserModule extends Module {
    @Override
    protected void initialize() {
        mongo().entityClass(User.class);
        mongo().viewClass(MongoUserAggregateView.class);

        bind(UserService.class);
    }
}
