package app.user.domain;

import core.framework.api.mongo.Field;

/**
 * @author neo
 */
public class Role {
    @Field(name = "name")
    public String name;
}
