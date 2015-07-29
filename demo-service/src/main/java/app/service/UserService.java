package app.service;

import app.domain.MongoUserAggregateView;
import app.domain.User;
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

    public User find(String id) {
        return mongo.findOne(User.class, new ObjectId(id)).get();
    }

    public List<MongoUserAggregateView> aggregate() {
        return mongo.aggregate(User.class, MongoUserAggregateView.class,
            BsonDocument.parse("{ $group: { _id: \"$status\", total: { $sum: \"$level\" } } }"));
    }
}
