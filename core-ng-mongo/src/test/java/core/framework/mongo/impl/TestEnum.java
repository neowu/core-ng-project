package core.framework.mongo.impl;

import core.framework.mongo.MongoEnumValue;

/**
 * @author neo
 */
public enum TestEnum {
    @MongoEnumValue("I1")
    ITEM1,
    @MongoEnumValue("I2")
    ITEM2
}
