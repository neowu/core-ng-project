package app.user.service;

import app.user.domain.MongoUserAggregateView;
import app.user.domain.Status;
import app.user.domain.User;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import core.framework.api.mongo.Mongo;
import org.bson.BsonDocument;
import org.bson.types.ObjectId;

import javax.inject.Inject;
import java.util.List;

/**
 * @author neo
 */
public class UserService {
    @Inject
    Mongo mongo;

    public void save(User user) {
        mongo.insert(user);
    }

    public User find(ObjectId id) {
        return mongo.findOne(User.class, id).get();
    }

    public List<User> findByStatus(Status status) {
        return mongo.find(User.class, Filters.eq("status", status.toString()), Sorts.ascending("name"), null, null);
    }

    public List<MongoUserAggregateView> aggregate() {
        return mongo.aggregate(User.class, MongoUserAggregateView.class,
            BsonDocument.parse("{ $group: { _id: \"$status\", total: { $sum: \"$level\" } } }"));
    }
}
