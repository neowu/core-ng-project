package app.service;

import app.IntegrationTest;
import app.domain.MongoUserAggregateView;
import app.domain.Status;
import app.domain.User;
import core.framework.api.mongo.Mongo;
import org.bson.BsonDocument;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author neo
 */
public class UserServiceTest extends IntegrationTest {
    @Inject
    UserService userService;
    @Inject
    Mongo mongo;

    @Before
    public void cleanup() {
        mongo.delete(User.class, new BsonDocument());
    }

    @Test
    public void save() {
        User user = new User();
        user.name = "test";
        userService.save(user);

        userService.save(user);

        User loadedUser = userService.find(user.id.toString());

        Assert.assertNotSame(user, loadedUser);
        Assert.assertEquals(user.name, loadedUser.name);
    }

    @Test
    public void aggregate() {
        User user = new User();
        user.name = "test1";
        user.level = 2;
        user.status = Status.ACTIVE;
        user.createdDate = LocalDateTime.now();
        userService.save(user);

        List<MongoUserAggregateView> results = userService.aggregate();
        Assert.assertEquals(1, results.size());
    }
}