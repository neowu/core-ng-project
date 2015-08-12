package app.domain;

import core.framework.api.mongo.Collection;
import core.framework.api.mongo.Field;
import core.framework.api.mongo.Id;
import core.framework.api.util.Lists;
import core.framework.api.util.Maps;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
@Collection(name = "users")
public class User {
    @Id
    public ObjectId id;
    @Field(name = "name")
    public String name;
    @Field(name = "level")
    public Integer level;
    @Field(name = "status")
    public Status status;
    @Field(name = "created_date")
    public LocalDateTime createdDate;

    @Field(name = "items")
    public List<String> items = Lists.newArrayList();

    @Field(name = "role")
    public Role role = new Role();

    @Field(name = "roles")
    public List<Role> roles = Lists.newArrayList();

    @Field(name = "map_roles")
    public Map<String, Role> roleMatrix = Maps.newLinkedHashMap();
}
