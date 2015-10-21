package app.user.service;

import app.IntegrationTest;
import app.user.domain.MongoUserAggregateView;
import app.user.domain.Status;
import app.user.domain.User;
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

        User loadedUser = userService.find(user.id);

        Assert.assertNotSame(user, loadedUser);
        Assert.assertEquals(user.name, loadedUser.name);
    }

    @Test
    public void findByStatus() {
        createUser("test3", Status.INACTIVE);
        createUser("test2", Status.ACTIVE);
        createUser("test1", Status.ACTIVE);

        List<User> users = userService.findByStatus(Status.ACTIVE);
        Assert.assertEquals(2, users.size());
        Assert.assertEquals("test1", users.get(0).name);
    }

    private void createUser(String name, Status status) {
        User user = new User();
        user.name = name;
        user.status = status;
        userService.save(user);
    }

    @Test
    public void aggregate() {
        User user = new User();
        user.name = "test";
        user.level = 2;
        user.status = Status.ACTIVE;
        user.createdDate = LocalDateTime.now();
        userService.save(user);

        List<MongoUserAggregateView> results = userService.aggregate();
        Assert.assertEquals(1, results.size());
    }
}